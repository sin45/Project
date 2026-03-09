// pages/addressManage/addressManage.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    ifPurchase: false,
    addressList: []
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    const ifPurchase =
      options &&
      (options.ifPurchase === true ||
        options.ifPurchase === 'true' ||
        options.ifPurchase === '1');
    this.setData({ ifPurchase: !!ifPurchase });
    this.loadAddressList();
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
    // 从地址详情页返回时，自动刷新地址列表
    this.loadAddressList();
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
   * 菜单页选择地址用
   */
  selectAddress(e) {
    const item = e && e.currentTarget && e.currentTarget.dataset
      ? e.currentTarget.dataset.item
      : null;

    if (this.data.ifPurchase && item) {
      const pages = getCurrentPages();
      const prevPage = pages[pages.length - 2];
      if (prevPage && typeof prevPage.setData === 'function') {
        prevPage.setData({
          selectedAddress: item,
          orderType: 'delivery',
        });
      }
      wx.navigateBack();
      return;
    } else {

      let query = JSON.stringify(item);
      query = encodeURIComponent(query);
      wx.navigateTo({ url: '/pages/addressDetail/addressDetail?data='+query });
    }

  },
  /**
   * 跳转到添加地址页
   */
  addAddress() {
    wx.navigateTo({ url: '/pages/addressDetail/addressDetail' });
  },

  /**
   * 加载地址列表
   */
  loadAddressList() {
    const userInfo = wx.getStorageSync('userInfo');
    const openId = userInfo ? userInfo.wxOpenid : '';
    if (!openId) {
      return;
    }
    const app = getApp();
    wx.request({
      url: app.apiUrl('/api/deliveryAddress/getDeliveryAddress'),
      method: 'POST',
      header: {
        'content-type': 'application/json'
      },
      data: { openId: openId },
      success: (serverRes) => {
        if (serverRes.data) {
          this.setData({
            addressList: serverRes.data
          });
        }
      }
    });
  }
})