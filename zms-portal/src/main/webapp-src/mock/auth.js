export default [
  {
    url: '/login',
    type: 'post',
    response: () => {
      return {
        result: {
          token: 'abcdefg'
        },
        status: true,
        statusCode: 'YS500',
        message: 'login'
      }
    }
  },
  {
    url: '/logout',
    type: 'get',
    response: () => {
      return {
        result: {},
        status: true,
        statusCode: 'YS500',
        message: 'logout'
      }
    }
  },
  {
    url: '/getUserInfo',
    type: 'get',
    response: () => {
      return {
        result: {
          fullname: '张三',
          nodeName: '上海',
          dept_name: '用户设计体验部',
          permissions: ['me_welcome'],
          menus: [
            {
              code: 'welcome',
              menu_url: 'common/welcome',
              menu_name: '首页',
              children: []
            },
            {
              menu_icon: 'fa fa-align-left',
              code: 'basedata',
              menu_url: '',
              menu_name: '基础数据',
              children: [
                {
                  menu_icon: '',
                  code: 'freight',
                  menu_url: 'basedata/freight',
                  menu_name: '货代维护',
                  children: []
                }
              ]
            }
          ]
        },
        status: true,
        statusCode: 'YS500',
        message: 'demoGet'
      }
    }
  }
]
