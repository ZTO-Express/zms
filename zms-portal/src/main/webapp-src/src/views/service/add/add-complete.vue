<template>
  <div class="service-add-complete">
    <p class="content_header">恭喜您！</p>
    <p style="font-size: 14px">您的新服务已安装在群集中并已配置。</p>
    <div class="content_border">
      <b>注意</b>
      <span>
        ：您可能仍必须启动新服务。建议您在执行此操作之前，先重启所有配置过时的依赖关系服务。可以通过单击下面的
      </span>
      <b>完成</b>
      <span>在主页面上执行这些操作</span>
    </div>
  </div>
</template>
<script>
export default {
  name: 'serviceAddComplete'
}
</script>
<style lang="scss">
.service-add-complete {
  margin: 0px auto;
  width: 820px;
  .content_header {
    font-size: 18px;
  }
  .content_border {
    margin-top: 20px;
    height: 30px;
    width: 820px;
    background-color: #eff0f8;
    padding-top: 16px;
    padding-left: 6px;
  }
}
</style>
