<script>
import TabNav from './TabNav'

export default {
  //tabs父级 和 鼠标右击菜单
  name: 'TabMenu',
  components: {
    TabNav
  },

  props: {
    panes: {
      type: Array,
      default: function() {
        return []
      }
    },
    currentName: {
      type: String,
      default: ''
    },
    currentParams: {
      type: Object,
      default: function() {
        return {}
      }
    },
    currentQuery: {
      type: Object,
      default: function() {
        return {}
      }
    }
  },

  data() {
    return {
      clientX: '0px',
      clientY: '0px',
      isShowMenu: false,
      curMouseOverTab: {}
    }
  },

  watch: {
    currentName() {
      this.$nextTick(() => {
        if (this.$refs.nav) this.$refs.nav.scrollToActiveTab()
      })
    }
  },
  created() {
    this.handlerDocClick()
  },
  methods: {
    handlerDocClick() {
      document.addEventListener('click', () => {
        this.isShowMenu = false
      })
    },
    closeOtherTab() {
      // 向父组件传出关掉其他标签栏的事件 传出为当前操作的标签栏
      this.$emit('close-other', this.curMouseOverTab)
    },
    closeAllTab() {
      // 向父组件传出关掉所有标签栏的事件  传出为当前操作的标签栏
      this.$emit('close-all', this.curMouseOverTab)
    },
    handleTabClick(tab, tabName, event) {
      // this.setCurrentName(tabName)
      this.$emit('tab-click', tab, event)
    },
    handleTabMouseDown(tab, tabName, event) {
      this.curMouseOverTab = tab
      // 鼠标按下事件传出给父组件的事件
      this.$emit('mouse-down', {
        tab: tab,
        tabName: tabName
      })
      if (event.button === 2) {
        // console.log(`y:${this.clientY},x:${this.clientX}`) for debug
        this.clientX = event.clientX + 10 + 'px'
        this.clientY = event.clientY + 'px'
        this.isShowMenu = true
      }
    },
    handleTabRemove(pane, ev) {
      ev.stopPropagation()
      this.$emit('tab-remove', pane)
    }
  },
  render() {
    const {
      handleTabClick,
      handleTabRemove,
      handleTabMouseDown,
      currentName,
      currentParams,
      currentQuery,
      closeOtherTab,
      closeAllTab,
      panes,
      isShowMenu,
      clientX,
      clientY
    } = this

    const navData = {
      props: {
        currentName,
        currentParams,
        currentQuery,
        onTabClick: handleTabClick,
        onTabRemove: handleTabRemove,
        onTabMouseDown: handleTabMouseDown,
        panes
      },
      ref: 'nav'
    }
    const showType = isShowMenu ? 'block' : 'none'
    return (
      <div
        class="el-tabs el-tabs--card"
        on-contextmenu={ev => {
          ev.preventDefault()
          ev.stopPropagation()
        }}>
        <div class="el-tabs__header">
          <TabNav {...navData}></TabNav>
        </div>
        <div style={{ display: showType, left: clientX, top: clientY }} class="tab-menu__popover">
          <ul>
            <li class="tab-menu__item" on-click={closeOtherTab}>
              关闭其它标签
            </li>
            <li class="tab-menu__item" on-click={closeAllTab}>
              关闭全部标签
            </li>
          </ul>
        </div>
      </div>
    )
  }
}
</script>

<style lang="scss">
.tab-menu {
  .el-tabs__header {
    margin: 0px;
    padding: 8px 0;
  }

  > .el-tabs__header .el-tabs__nav {
    border: none;
  }

  .el-tabs__item {
    padding: 0 30px;
    line-height: 30px;
    height: 30px;
  }

  .el-tabs__item.is-active {
    color: #47a6ff;
  }
  > .el-tabs__header .el-tabs__item .el-icon-circle-close {
    position: absolute;
    font-size: 18px;
    color: #ea5524;
    cursor: pointer;
    display: none;
    right: 6px;
  }

  > .el-tabs__header .el-tabs__item.is-closable:hover {
    padding: 0 30px;
    .el-icon-circle-close {
      display: initial;
    }
  }
  > .el-tabs__header .el-tabs__item.is-active.is-closable {
    padding: 0 30px;
  }
  .el-tabs__nav-prev,
  .el-tabs__nav-next {
    display: flex;
    line-height: 30px;
    height: 30px;
    align-items: center;
  }

  .el-tabs__nav-prev {
    padding: 0 5px 0 15px;
  }

  .el-tabs__nav-next {
    padding: 0 15px 0 5px;
  }

  .el-icon-arrow-left,
  .el-icon-arrow-right {
    border: 1px solid #47a6ff;
    border-radius: 50%;
    padding: 2px;
    color: #47a6ff;
    font-size: 12px;
  }

  .el-tabs__nav-wrap.is-scrollable {
    padding: 0 38px;
  }
  .tab-menu__popover {
    ul,
    li {
      list-style: none;
      padding: 0;
      margin: 0;
    }
    position: fixed;
    cursor: pointer;
    background: #ffffff;
    z-index: 10000;
    box-shadow: 0px 2px 4px 2px #eeeeee;
    .tab-menu__item {
      padding: 10px 20px;
      font-size: 14px;
      &:hover {
        background: #f4f3f3;
      }
    }
  }
}
.tab-menu--shadow {
  position: relative;
  z-index: 1000;
  box-shadow: 0 0 10px 0 rgba(123, 123, 123, 1);
}
</style>
