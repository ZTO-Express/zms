<script>
/* import { addResizeListener, removeResizeListener } from 'element-ui/src/utils/resize-event';*/
import { addResizeListener, removeResizeListener } from './resize-event'
function noop() {}

export default {
  name: 'TabNav',
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
    },
    onTabClick: {
      type: Function,
      default: noop
    },
    onTabMouseDown: {
      type: Function,
      default: noop
    },
    onTabRemove: {
      type: Function,
      default: noop
    }
  },

  data() {
    return {
      scrollable: false,
      navStyle: {
        transform: ''
      }
    }
  },
  mounted() {
    addResizeListener(this.$el, this.update)
  },
  updated() {
    this.update()
  },
  beforeDestroy() {
    if (this.$el && this.update) removeResizeListener(this.$el, this.update)
  },
  methods: {
    scrollPrev() {
      const containerWidth = this.$refs.navScroll.offsetWidth
      const currentOffset = this.getCurrentScrollOffset()

      if (!currentOffset) return

      const newOffset = currentOffset > containerWidth ? currentOffset - containerWidth : 0

      this.setOffset(newOffset)
    },
    scrollNext() {
      const navWidth = this.$refs.nav.offsetWidth
      const containerWidth = this.$refs.navScroll.offsetWidth
      const currentOffset = this.getCurrentScrollOffset()

      if (navWidth - currentOffset <= containerWidth) return

      const newOffset =
        navWidth - currentOffset > containerWidth * 2 ? currentOffset + containerWidth : navWidth - containerWidth

      this.setOffset(newOffset)
    },
    scrollToActiveTab() {
      if (!this.scrollable) return
      const nav = this.$refs.nav
      const activeTab = this.$el.querySelector('.is-active')
      const navScroll = this.$refs.navScroll
      const activeTabBounding = activeTab.getBoundingClientRect()
      const navScrollBounding = navScroll.getBoundingClientRect()
      const navBounding = nav.getBoundingClientRect()
      const currentOffset = this.getCurrentScrollOffset()
      let newOffset = currentOffset

      if (activeTabBounding.left < navScrollBounding.left) {
        newOffset = currentOffset - (navScrollBounding.left - activeTabBounding.left)
      }
      if (activeTabBounding.right > navScrollBounding.right) {
        newOffset = currentOffset + activeTabBounding.right - navScrollBounding.right
      }
      if (navBounding.right < navScrollBounding.right) {
        newOffset = nav.offsetWidth - navScrollBounding.width
      }
      this.setOffset(Math.max(newOffset, 0))
    },
    getCurrentScrollOffset() {
      const { navStyle } = this
      return navStyle.transform ? Number(navStyle.transform.match(/translateX\(-(\d+(\.\d+)*)px\)/)[1]) : 0
    },
    setOffset(value) {
      this.navStyle.transform = `translateX(-${value}px)`
    },
    update() {
      const navWidth = this.$refs.nav.offsetWidth
      const containerWidth = this.$refs.navScroll.offsetWidth
      const currentOffset = this.getCurrentScrollOffset()

      if (containerWidth < navWidth) {
        const currentOffset = this.getCurrentScrollOffset()
        this.scrollable = this.scrollable || {}
        this.scrollable.prev = currentOffset
        this.scrollable.next = currentOffset + containerWidth < navWidth
        if (navWidth - currentOffset < containerWidth) {
          this.setOffset(navWidth - containerWidth)
        }
      } else {
        this.scrollable = false
        if (currentOffset > 0) {
          this.setOffset(0)
        }
      }
    }
  },

  render() {
    const {
      panes,
      onTabClick,
      onTabMouseDown,
      onTabRemove,
      navStyle,
      scrollable,
      scrollNext,
      scrollPrev,
      currentName,
      currentParams,
      currentQuery
    } = this

    let scrollBtn = [
      <span class={['el-tabs__nav-prev', scrollable.prev ? '' : 'is-disabled']} on-click={scrollPrev}>
        <i class="el-icon-arrow-left" />
      </span>,
      <span class={['el-tabs__nav-next', scrollable.next ? '' : 'is-disabled']} on-click={scrollNext}>
        <i class="el-icon-arrow-right" />
      </span>
    ]
    if (!scrollable) scrollBtn = null

    const tabs = this._l(panes, (pane, index) => {
      const tabName = pane.name || pane.index || index
      const closable = pane.closable

      pane.index = `${index}`

      const btnClose = closable ? (
        <span
          class="el-icon-circle-close"
          on-click={ev => {
            onTabRemove(pane, ev)
          }}
        />
      ) : null

      const tabLabelContent = pane.label
      const isactive =
        pane.name === currentName &&
        JSON.stringify(pane.params) === JSON.stringify(currentParams) &&
        JSON.stringify(pane.query) === JSON.stringify(currentQuery)

      return (
        <div
          class={{
            'el-tabs__item': true,
            'is-active': isactive,
            'is-disabled': pane.disabled,
            'is-closable': closable
          }}
          ref="tabs"
          refInFor
          on-click={ev => {
            onTabClick(pane, tabName, ev)
          }}
          on-mousedown={ev => {
            onTabMouseDown(pane, tabName, ev)
          }}>
          {tabLabelContent}
          {btnClose}
        </div>
      )
    })
    return (
      <div class={['el-tabs__nav-wrap', scrollable ? 'is-scrollable' : '']}>
        {scrollBtn}
        <div class={['el-tabs__nav-scroll']} ref="navScroll">
          <div class="el-tabs__nav" ref="nav" style={navStyle}>
            {tabs}
          </div>
        </div>
      </div>
    )
  }
}
</script>
