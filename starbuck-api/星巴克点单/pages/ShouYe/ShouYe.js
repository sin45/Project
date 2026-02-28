// pages/ShouYe/ShouYe.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    ifManage: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {

  },

  handleBannerClick: function(event) {
    const type = event.currentTarget.dataset.type;
    console.log('点击了:', type);
    //传递参数 - 外送另外处理
    wx.navigateTo({
      url: '/pages/Purchase/Purchase'
    });
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
    const userInfo = wx.getStorageSync('userInfo');
    const openId = userInfo.wxOpenid;
    console.log("我的openid：", openId);
    wx.request({
      url: 'http://230791mi80.51mypc.cn/api/wxManage/getManageOpenId',
      method: 'POST',
      header: {
        'content-type': 'application/json'
      },
      data: { openId: openId },
      success: (serverRes) => {
        if(serverRes.data){
          this.setData({
            ifManage: true
          })
        }
      }
    });
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
    
    wx.requestSubscribeMessage({
      tmplIds: ["cU0fxhT7E3hxUxRScQ0QHqLGMaEQQMNDEDgtr4BIXHY"],
      success (res) {
        console.log("订阅结果：", res)
      }
    });
    
    wx.navigateTo({ url: '/pages/orderManage/orderManage' });
  }
})