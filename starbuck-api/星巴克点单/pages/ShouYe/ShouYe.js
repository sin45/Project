// pages/ShouYe/ShouYe.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    ifManage: true
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {

  },

  handleBannerClick: function(event) {
    const type = event.currentTarget.dataset.type;
    console.log('点击了:', type);
    
    if (type === '啡快' || type === '专星送') {
      wx.navigateTo({
        url: '/pages/Purchase/Purchase'
      });
    }
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },
  
  /**
   * 订单管理页
   */
  showOrderManage() {
    wx.navigateTo({ url: '/pages/orderManage/orderManage' });
  }
})