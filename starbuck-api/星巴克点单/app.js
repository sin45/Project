App({
  onLaunch() {
    //用户信息为空，尝试登陆
    const userInfo = wx.getStorageSync('userInfo');
    if(userInfo === null || userInfo === undefined || userInfo === ''){
      wx.login({
        success: (res) => {
          if (res.code) {
            console.log('获取到的code:', res.code);
            wx.request({
              url: 'http://localhost:8080/api/users/login/wechat',
              method: 'POST',
              header: {
                'content-type': 'application/json'
              },
              data: { code: res.code },
              success: (serverRes) => {
                console.log('获取到的userInfo:', serverRes.data.user);
                wx.setStorageSync('userInfo', serverRes.data.user);
                this.globalData.userInfo = userInfo;
              }
            });
          } else {
            console.log('登录失败:', res.errMsg);
          }
        }
      });
    }

    // 初始化购物车
    if (!this.globalData.cartItems) {
      this.globalData.cartItems = [];
    }
  },
  globalData: {
    cartItems: [],
    userInfo: ""
  },
  getCartItems() {
    return this.globalData.cartItems || [];
  },
  addToCart(item) {
    const cart = this.globalData.cartItems || [];
    cart.push(item);
    this.globalData.cartItems = cart;
    return cart.length;
  },
  calculateCartTotal() {
    const cart = this.globalData.cartItems || [];
    let totalQuantity = 0;
    let totalPrice = 0;
    cart.forEach(item => {
      totalQuantity += item.quantity || 1;
      totalPrice += (item.price || 0) * (item.quantity || 1);
    });
    return { totalQuantity, totalPrice };
  }
});