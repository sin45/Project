// pages/DingDan/DingDan.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    // 订单数据
    hasOrder: false,
    orderList: [],
    allOrders: []
  },
  
  onShow() {
    // 登录态校验
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.userId) {
      // 未登录，显示暂无订单，不跳转
      this.setData({ hasOrder: false, orderList: [], allOrders: [] });
      return;
    }
    this.loadOrderData(userInfo.userId);
  },

  // 加载订单数据
  loadOrderData: function(userId) {
    wx.showLoading({ title: '加载中...' });
    const app = getApp();
    wx.request({
      url: app.apiUrl(`/api/orders?userId=${userId}`),
      method: 'GET',
      success: (res) => {
        if (res.statusCode === 200) {
          const orderList = (res.data || []).map(order => {
            order.totalAmountStr = order.totalAmount != null ? Number(order.totalAmount).toFixed(2) : '0.00';
            if (order.orderDetails) {
              order.orderDetails = order.orderDetails.map(detail => ({
                ...detail,
                unitPriceStr: detail.unitPrice != null ? Number(detail.unitPrice).toFixed(2) :
                              (detail.price != null ? Number(detail.price).toFixed(2) : '')
              }));
            }
            return order;
          });
          // 保存全部订单，初始筛选
          this.setData({
            allOrders: orderList
          });
          console.log('格式化后订单数据:', orderList);
          console.log('订单列表长度:', orderList.length);
          if (orderList.length > 0) {
            this.setData({
              hasOrder: true,
              orderList: orderList
            });
            console.log('第一个订单的orderId:', orderList[0].orderId);
          }
        } else {
          wx.showToast({ title: '加载失败', icon: 'none' });
          this.setData({ hasOrder: false, orderList: [], allOrders: [] });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' });
        this.setData({ hasOrder: false, orderList: [], allOrders: [] });
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },
  
  // 跳转到点单页面
  goToOrderPage: function() {
    wx.navigateTo({
      url: '/pages/Purchase/Purchase'
    });
  },

  //订单详情跳转
  handleBannerClick: function(event) {
    console.log('handleBannerClick 被调用');
    console.log('事件对象:', event);
    console.log('dataset:', event.currentTarget.dataset);
    
    const orderId = event.currentTarget.dataset.orderId;
    console.log('点击了订单:', orderId);
    
    if (!orderId) {
      console.error('订单ID为空');
      wx.showToast({
        title: '订单ID无效',
        icon: 'none'
      });
      return;
    }
    
    console.log('准备跳转到订单详情页面，orderId:', orderId);
    
    wx.navigateTo({
      url: `/pages/OrderDetail/OrderDetail?orderId=${orderId}`,
      success: () => {
        console.log('跳转成功');
      },
      fail: (err) => {
        console.error('跳转失败:', err);
        wx.showToast({
          title: '跳转失败',
          icon: 'none'
        });
      }
    });
  },

  // 再来一单功能
  reorder: function(event) {
    // 阻止事件冒泡，避免触发父元素的点击事件
    event.stopPropagation();
    
    const orderId = event.currentTarget.dataset.id;
    console.log('再来一单，订单ID:', orderId);
    
    // 跳转到 Purchase 页面并传递订单ID
    wx.navigateTo({
      url: `/pages/Purchase/Purchase?orderId=${orderId}`
    });
    
    // 这里可以添加再来一单的逻辑
    // 比如跳转到点单页面并预填充商品
  }

})