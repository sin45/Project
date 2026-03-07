const app = getApp();

Page({
  data: {
    userInfo: null           // 用户数据
  },

  // 页面第一次加载时检查登陆状态
  onLoad() {
    const userInfo = app.globalData.userInfo;
    //用户信息为空，尝试登陆
    if(userInfo === null || userInfo === undefined || userInfo === ''){
      wx.login({
        success: (res) => {
          if (res.code) {
            console.log('获取到的code:', res.code);
            wx.request({
              url: app.apiUrl('/api/users/login/wechat'),
              method: 'POST',
              header: {
                'content-type': 'application/json'
              },
              data: { code: res.code },
              success: (serverRes) => {
                console.log('获取到的userInfo:', serverRes.data.user);
                wx.setStorageSync('userInfo', serverRes.data.user);
                this.setData({
                  userInfo: serverRes.data.user
                });
              }
            });
          } else {
            console.log('登录失败:', res.errMsg);
          }
        }
      });
    }
  },

  // 页面显示时检查登录状态，并在需要时刷新后端数据
  onShow() {
    // const userInfo = wx.getStorageSync('userInfo');

    // const userId = userInfo && (userInfo.userId || userInfo.user_id || userInfo.id);
    // const needRefresh = wx.getStorageSync('refreshUserInfo');
    // if (userInfo && userId && needRefresh) {
    //   this.getUserInfoFromServer(userId);
    //   wx.removeStorageSync('refreshUserInfo'); // 刷新后清除标志
    // } else if (userInfo && userId) {
    //   this.setData({
    //     isLoggedIn: true,
    //     userInfo
    //   });
    // } else {
    //   this.setData({
    //     isLoggedIn: false,
    //     userInfo: null
    //   });
    // }
  },

  // 实时拉取后端用户信息
  // getUserInfoFromServer(userId) {
  //   wx.request({
  //     url: `http://localhost:8080/api/users/${userId}`,
  //     method: 'GET',
  //     success: (res) => {
  //       const user = res.data && (res.data.user || res.data);
  //       const id = user && (user.userId || user.user_id || user.id);
  //       if (user && id) {
  //         wx.setStorageSync('userInfo', user);
  //         this.setData({
  //           isLoggedIn: true,
  //           userInfo: user
  //         });
  //       } else {
  //         this.setData({
  //           isLoggedIn: false,
  //           userInfo: null
  //         });
  //       }
  //     }
  //   });
  // },

  // 跳转到个人信息页
  onProfile() {
    wx.navigateTo({ url: '/pages/userInfo/userInfo' });
  },

  // 跳转到订单页
  onOrderCenter() {
    wx.navigateTo({ url: '/pages/DingDan/DingDan' });
  },

  // 跳转到地址管理
  onTickets() {
    wx.navigateTo({ url: '/pages/addressManage/addressManage' });
  },

  // 跳转到门店活动
  onStoreActivity() {
    wx.navigateTo({ url: '/pages/storeActivity/storeActivity' });
  },

  // 联系客服
  onService() {
    const phoneNumber = '10086'; // 替换成要拨打的电话号码
    wx.makePhoneCall({
      phoneNumber: phoneNumber
    })
  }
});