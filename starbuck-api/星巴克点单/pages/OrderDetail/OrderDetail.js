Page({
  // 页面的初始数据
  data: {
    orderDetails: [],
    orderId: null,
    orderStatus: '处理中',
    totalAmount: '0.00',
    totalItems: 0,
    debugJson: '' // 用于调试输出
  },

  /* 生命周期函数--监听页面加载*/
  onLoad(options) {
    console.log('OrderDetail onLoad options:', options);
    
    // 获取 orderId（页面跳转时传递，如 ?orderId=1）
    let orderId = options.orderId || options.id;
    
    // 确保 orderId 是数字类型
    if (orderId) {
      orderId = parseInt(orderId);
    }
    
    // 如果没有有效的 orderId，使用默认值
    if (!orderId || isNaN(orderId)) {
      orderId = 1;
      console.warn('No valid orderId provided, using default: 1');
    }
    
    console.log('Using orderId:', orderId);
    
    this.setData({ orderId });
    this.fetchOrderDetails(orderId);
  },

  //地址跳转页面
  navigateToPage: function () {
    wx.navigateTo({
      url: '' // 替换为目标页面的路径
    });
  },

  scanQRCode: function () {
    // 这里可以写长按识别二维码的逻辑
    wx.showToast({
      title: '长按识别二维码',
      icon: 'none'
    });
  },

  fetchOrderDetails(orderId) {
    console.log('Fetching order details for orderId:', orderId);
    wx.showLoading({ title: '加载中...' });
    const app = getApp();
    wx.request({
      url: app.apiUrl(`/api/orderdetail/${orderId}`),
      method: 'GET',
      success: (res) => {
        wx.hideLoading();
        console.log('API response:', res);
        
        // 输出原始返回内容用于调试
        this.setData({ debugJson: JSON.stringify(res, null, 2) });
        
        if (res.statusCode === 200 && Array.isArray(res.data)) {
          // 格式化金额和计算总计
          let totalAmount = 0;
          let totalItems = 0;
          
          const details = res.data.map(item => {
            const unitPrice = item.unitPrice ? Number(item.unitPrice) : 0;
            const subtotal = item.subtotal ? Number(item.subtotal) : 0;
            const quantity = item.quantity || 0;
            
            totalAmount += subtotal;
            totalItems += quantity;
            
            return {
              ...item,
              unit_price_fmt: unitPrice.toFixed(2),
              subtotal_fmt: subtotal.toFixed(2)
            };
          });
          
          this.setData({ 
            orderDetails: details,
            totalAmount: totalAmount.toFixed(2),
            totalItems: totalItems
          });
          
          console.log('Processed order details:', details);
        } else {
          console.error('Invalid response format:', res);
          wx.showToast({ 
            title: '数据格式错误', 
            icon: 'none' 
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('Request failed:', err);
        
        // 输出错误对象用于调试
        this.setData({ debugJson: JSON.stringify(err, null, 2) });
        
        wx.showToast({ 
          title: '加载订单明细失败', 
          icon: 'none' 
        });
      }
    });
  },

  // 复制订单号
  copyOrderNumber: function () {
    wx.setClipboardData({
      data: this.data.orderId.toString(),
      success: function () {
        wx.showToast({
          title: '复制成功',
          icon: 'success'
        });
      }
    });
  },

  // 联系门店
  contactStore: function () {
    wx.makePhoneCall({
      phoneNumber: '400-820-6998', // 星巴克客服电话
      fail: () => {
        wx.showToast({
          title: '拨打电话失败',
          icon: 'none'
        });
      }
    });
  },
  
  // 联系客服
  contactService: function () {
    wx.showToast({
      title: '正在连接客服...',
      icon: 'success'
    });
    // 这里可以添加跳转到客服页面的逻辑
  },

  /*生命周期函数--监听页面初次渲染完成*/
  onReady() {
    console.log('OrderDetail page ready');
  },

  /*生命周期函数--监听页面显示*/
  onShow() {
    console.log('OrderDetail page show');
  },

  /* 生命周期函数--监听页面隐藏*/
  onHide() {
    console.log('OrderDetail page hide');
  },

  /* 生命周期函数--监听页面卸载*/
  onUnload() {
    console.log('OrderDetail page unload');
  },

  /* 页面相关事件处理函数--监听用户下拉动作*/
  onPullDownRefresh() {
    // 重新加载数据
    this.fetchOrderDetails(this.data.orderId);
    wx.stopPullDownRefresh();
  },

  /*页面上拉触底事件的处理函数*/
  onReachBottom() {
    // 可以在这里添加加载更多数据的逻辑
  },

  /*用户点击右上角分享*/
  onShareAppMessage() {
    return {
      title: '我的星巴克订单',
      path: `/pages/OrderDetail/OrderDetail?orderId=${this.data.orderId}`
    };
  }
})