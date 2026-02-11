App({
  onLaunch() {
    // 静默检查登录状态（不强制跳转）
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.globalData.userInfo = userInfo;
    }
    // 初始化购物车
    if (!this.globalData.cartItems) {
      this.globalData.cartItems = [];
    }
  },
  globalData: {
    cartItems: []
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