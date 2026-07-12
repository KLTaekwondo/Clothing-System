<template>
  <div class="maincontainer">
    <div class="header">
      <h2>服装管理系统</h2>
    </div>

    <nav class="nav-menu">
      <router-link to="/test" class="nav-item">
        <SmallOption icon="zhexiantu" title="数据看板" />
      </router-link>
      <router-link to="/test" class="nav-item">
        <SmallOption icon="yifu" title="服装状态" />
      </router-link>
      <SmallCollapsible icon="gouwu" title="采购管理">
        <router-link to="/test" class="nav-item">
          <SmallOption icon="gouwu" title="采购单" />
        </router-link>
        <router-link to="/test" class="nav-item">
          <SmallOption icon="gouwu" title="采购退货单" />
        </router-link>
      </SmallCollapsible>
      <SmallCollapsible icon="gouwu" title="调货管理">
        <router-link to="/test" class="nav-item">
          <SmallOption icon="gouwu" title="调拨出货单" />
        </router-link>
        <router-link to="/test" class="nav-item">
          <SmallOption icon="gouwu" title="调拨入货单" />
        </router-link>
      </SmallCollapsible>
      <router-link to="/test" class="nav-item">
        <SmallOption icon="gouwu" title="商品目录" />
      </router-link>
      <SmallCollapsible icon="qunzu" title="用户管理">
        <router-link to="/WareHouseManage" class="nav-item">
          <SmallOption icon="cangku" title="仓铺管理" />
        </router-link>
        <router-link to="/SalesManManage" class="nav-item">
          <SmallOption icon="xiaoshouyuan" title="销售员管理" />
        </router-link>
      </SmallCollapsible>
      <!-- 其他菜单项 -->
    </nav>
  </div>
</template>

<script setup>
import SmallOption from './SmallOption.vue'
import SmallCollapsible from './SmallCollapsible.vue';
</script>

<style scoped>
.maincontainer {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  text-align: center;
  padding: 0 10px;
  position: relative;
  z-index: 2;
}

/* 增强h2的立体感，保留原有渐变但增加3D效果 */
.header h2 {
  flex: 1;
  font-size: 20px;
  height: 50px;
  margin: 15px 0;
  color: white;
  background: linear-gradient(135deg, #1890ff 0%, #52c41a 100%);
  border-radius: 12px;
  line-height: 50px;
  position: relative;
  /* 立体阴影效果 */
  box-shadow: 
    0 8px 20px rgba(24, 144, 255, 0.3),
    0 4px 8px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    inset 0 -1px 0 rgba(0, 0, 0, 0.1);
  /* 边框增强立体感 */
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  /* 微妙的文字浮雕效果 */
  text-shadow: 
    0 1px 0 rgba(255, 255, 255, 0.3),
    0 -1px 0 rgba(0, 0, 0, 0.2);
}

/* 为h2添加光晕效果 */
.header h2::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, #1890ff 0%, #52c41a 100%);
  border-radius: 14px;
  z-index: -1;
  opacity: 0.6;
  filter: blur(8px);
}


.nav-menu {
  flex: 1;
  overflow-y: auto;
  padding: 10px 8px;
  background: inherit;
  box-shadow: 
    inset 3px 3px 8px rgba(0, 0, 0, 0.1),
    inset -3px -3px 8px rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  margin: 0 8px 8px 8px;
}

.nav-item {
  text-decoration: none;
  color: inherit;
}

.nav-item:hover {
  text-decoration: none;
}

.nav-item:active {
  text-decoration: none;
}

.nav-item:visited {
  text-decoration: none;
}
</style>