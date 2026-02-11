Page({
  data: {
    username: '',
    password: '',
    loading: false,
    from: 'user' // 默认来源
  },

  // 页面加载时记录来源参数
  onLoad(options) {
    this.setData({
      from: options.from || 'user'
    });
  },

  // 输入处理
  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
  },

   // 跳转注册页
   navigateToRegister() {
    wx.navigateTo({ url: '/pages/register/register' });
  },

  // 登录接口调用
  handleLogin() {
    const { username, password, from } = this.data;
    if (!username || !password) {
      wx.showToast({ title: '请输入完整信息', icon: 'none' });
      return;
    }

    this.setData({ loading: true });

    wx.request({
      url: 'http://localhost:8080/api/users/login', // 后端接口
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: { username, password },
      success: (res) => {
        if (res.data.success) {
          wx.setStorageSync('userInfo', res.data.user);
          console.log('登录成功，userInfo:', JSON.stringify(res.data.user, null, 2)); // 打印JSON格式
          wx.showToast({ title: '登录成功', icon: 'success' });
          // 根据来源跳转
          if (from === 'checkout') {
            wx.redirectTo({ url: '/pages/Checkout/Checkout' });
          } else {
            wx.redirectTo({ url: '/pages/user/user' });
          }
        } else {
          wx.showToast({ title: res.data.message || '用户名或密码错误', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络请求失败', icon: 'none' });
      },
      complete: () => this.setData({ loading: false })
    });
  }
});