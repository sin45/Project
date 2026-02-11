// pages/addressManage/addressManage.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    addressList: [
      {
        id: 1,
        address1: "水岸人家",
        address2: "13号楼301",
        receiver: "王先生",
        phone: "17823497362"
      },
      {
        id: 2,
        address1: "金长城花园",
        address2: "1号楼301",
        receiver: "李小姐",
        phone: "15223497300"
      }
    ]
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {

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
   * 跳转到添加地址页
   */
  addAddress() {
    wx.navigateTo({ url: '/pages/addressDetail/addressDetail' });
  }
})