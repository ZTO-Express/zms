/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zto.zms.service.influx;

import org.influxdb.InfluxDBMapperException;
import org.influxdb.annotation.Column;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InfluxdbModelTransformer {

    public static final Logger logger = LoggerFactory.getLogger(InfluxdbModelTransformer.class);

    public static class InstanceHolder {
        static InfluxdbModelTransformer transformer = new InfluxdbModelTransformer();

        public static InfluxdbModelTransformer getInstance() {
            return transformer;
        }
    }


    private static final ConcurrentMap<String, ConcurrentMap<String, Field>> CLASS_FIELD_CACHE = new ConcurrentHashMap<>();
    private static final int FRACTION_MIN_WIDTH = 0;
    private static final int FRACTION_MAX_WIDTH = 6;
    private static final boolean ADD_DECIMAL_POINT = true;

    private static final DateTimeFormatter ISO8601_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.MICRO_OF_SECOND, FRACTION_MIN_WIDTH, FRACTION_MAX_WIDTH, ADD_DECIMAL_POINT)
            .appendPattern("X").toFormatter();

    public <T> T toPOJO(final QueryResult queryResult, final Class<T> clazz)
            throws InfluxDBMapperException {
        List<T> results = toPOJOList(queryResult, clazz);
        if (results == null || results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    public <T> List<T> toPOJOList(final QueryResult queryResult, final Class<T> clazz)
            throws InfluxDBMapperException {
        Objects.requireNonNull(queryResult, "queryResult");
        Objects.requireNonNull(clazz, "clazz");

        throwExceptionIfResultWithError(queryResult);
        cacheMeasurementClass(clazz);

        List<T> result = new LinkedList<T>();
        queryResult.getResults().stream()
                .filter(internalResult -> Objects.nonNull(internalResult) && Objects.nonNull(internalResult.getSeries()))
                .forEach(internalResult -> internalResult.getSeries().forEach(series -> parseSeriesAs(series, clazz, result)));
        return result;
    }

    void throwExceptionIfResultWithError(final QueryResult queryResult) {
        if (queryResult.getError() != null) {
            throw new InfluxDBMapperException("InfluxDB returned an error: " + queryResult.getError());
        }

        queryResult.getResults().forEach(seriesResult -> {
            if (seriesResult.getError() != null) {
                throw new InfluxDBMapperException("InfluxDB returned an error with Series: " + seriesResult.getError());
            }
        });
    }

    void cacheMeasurementClass(final Class<?>... classVarAgrs) {
        for (Class<?> clazz : classVarAgrs) {
            if (CLASS_FIELD_CACHE.containsKey(clazz.getName())) {
                continue;
            }
            ConcurrentMap<String, Field> initialMap = new ConcurrentHashMap<>();
            ConcurrentMap<String, Field> influxColumnAndFieldMap = CLASS_FIELD_CACHE.putIfAbsent(clazz.getName(), initialMap);
            if (influxColumnAndFieldMap == null) {
                influxColumnAndFieldMap = initialMap;
            }

            for (Field field : clazz.getDeclaredFields()) {
                Column colAnnotation = field.getAnnotation(Column.class);
                if (colAnnotation != null) {
                    influxColumnAndFieldMap.put(colAnnotation.name(), field);
                }
            }
        }
    }

    <T> void parseSeriesAs(final QueryResult.Series series, final Class<T> clazz, final List<T> result) {
        int columnSize = series.getColumns().size();
        try {
            T object = null;
            for (List<Object> row : series.getValues()) {
                for (int i = 0; i < columnSize; i++) {
                    String resultColumnName = series.getColumns().get(i);
                    Field correspondingField = CLASS_FIELD_CACHE.get(clazz.getName()).get(resultColumnName);
                    if (correspondingField != null) {
                        if (object == null) {
                            object = clazz.newInstance();
                        }
                        setFieldValue(object, correspondingField, row.get(i));
                    }
                }
                if (series.getTags() != null) {
                    for (Entry<String, String> tag : series.getTags().entrySet()) {
                        Field correspondingField = CLASS_FIELD_CACHE.get(clazz.getName()).get(tag.getKey());
                        if (correspondingField != null) {
                            if (object == null) {
                                object = clazz.newInstance();
                            }
                            setFieldValue(object, correspondingField, tag.getValue());
                        }
                    }
                }
                if (object != null) {
                    result.add(object);
                    object = null;
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("parse error ", e);
            throw new InfluxDBMapperException(e);
        }
    }

    <T> void setFieldValue(final T object, final Field field, final Object value)
            throws IllegalArgumentException, IllegalAccessException {
        if (value == null) {
            return;
        }
        Class<?> fieldType = field.getType();
        boolean oldAccessibleState = field.isAccessible();
        try {
            field.setAccessible(true);
            if (fieldValueModified(fieldType, field, object, value)
                    || fieldValueForPrimitivesModified(fieldType, field, object, value)
                    || fieldValueForPrimitiveWrappersModified(fieldType, field, object, value)) {
                return;
            }
            String msg = "Class '%s' field '%s' is from an unsupported type '%s'.";
            throw new InfluxDBMapperException(
                    String.format(msg, object.getClass().getName(), field.getName(), field.getType()));
        } catch (ClassCastException e) {
            String msg = "Class '%s' field '%s' was defined with a different field type and caused a ClassCastException. "
                    + "The correct type is '%s' (current field value: '%s').";
            throw new InfluxDBMapperException(
                    String.format(msg, object.getClass().getName(), field.getName(), value.getClass().getName(), value));
        } finally {
            field.setAccessible(oldAccessibleState);
        }
    }

    <T> boolean fieldValueModified(final Class<?> fieldType, final Field field, final T object, final Object value)
            throws IllegalArgumentException, IllegalAccessException {
        if (String.class.isAssignableFrom(fieldType)) {
            field.set(object, String.valueOf(value));
            return true;
        }
        if (Instant.class.isAssignableFrom(fieldType)) {
            Instant instant;
            if (value instanceof String) {
                instant = Instant.from(ISO8601_FORMATTER.parse(String.valueOf(value)));
            } else if (value instanceof Long) {
                instant = Instant.ofEpochMilli((Long) value);
            } else if (value instanceof Double) {
                instant = Instant.ofEpochMilli(((Double) value).longValue());
            } else {
                throw new InfluxDBMapperException("Unsupported type " + field.getClass() + " for field " + field.getName());
            }
            field.set(object, instant);
            return true;
        }
        return false;
    }

    <T> boolean fieldValueForPrimitivesModified(final Class<?> fieldType, final Field field, final T object,
                                                final Object value) throws IllegalArgumentException, IllegalAccessException {
        if (double.class.isAssignableFrom(fieldType)) {
            field.setDouble(object, (Double) value);
            return true;
        }
        if (long.class.isAssignableFrom(fieldType)) {
            field.setLong(object, ((Double) value).longValue());
            return true;
        }
        if (int.class.isAssignableFrom(fieldType)) {
            field.setInt(object, ((Double) value).intValue());
            return true;
        }
        if (boolean.class.isAssignableFrom(fieldType)) {
            field.setBoolean(object, Boolean.parseBoolean(String.valueOf(value)));
            return true;
        }
        return false;
    }

    <T> boolean fieldValueForPrimitiveWrappersModified(final Class<?> fieldType, final Field field, final T object,
                                                       final Object value) throws IllegalArgumentException, IllegalAccessException {
        if (Double.class.isAssignableFrom(fieldType)) {
            field.set(object, value);
            return true;
        }
        if (Long.class.isAssignableFrom(fieldType)) {
            field.set(object, ((Double) value).longValue());
            return true;
        }
        if (Integer.class.isAssignableFrom(fieldType)) {
            field.set(object, ((Double) value).intValue());
            return true;
        }
        if (Boolean.class.isAssignableFrom(fieldType)) {
            field.set(object, Boolean.valueOf(String.valueOf(value)));
            return true;
        }
        return false;
    }
}

