Page({
  data: {
    username: '',
    password: '',
    userPhone: '',
    usernameAvailable: null,
    phoneAvailable: null,
    loading: false
  },
  
  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
    // 输入变化时重置校验状态
    if (field === 'username') {
      this.setData({ usernameAvailable: null });
    }
    if (field === 'userPhone') {
      this.setData({ phoneAvailable: null });
    }
  },

  // 失去焦点时校验用户名唯一性
  onUsernameBlur(e) {
    const username = e.detail.value;
    if (username) this.checkUsername(username);
  },
  // 失去焦点时校验手机号唯一性
  onPhoneBlur(e) {
    const phone = e.detail.value;
    if (phone) this.checkPhone(phone);
  },

  // 校验用户名唯一性
  checkUsername(username) {
    wx.request({
      url: `http://localhost:8080/api/users/check-username?username=${encodeURIComponent(username)}`,
      method: 'GET',
      success: (res) => {
        this.setData({ usernameAvailable: res.data.available });
        if (!res.data.available) {
          wx.showToast({ title: '用户名已存在', icon: 'none' });
        }
      }
    });
  },

  // 校验手机号唯一性
  checkPhone(phone) {
    wx.request({
      url: `http://localhost:8080/api/users/check-phone?phone=${encodeURIComponent(phone)}`,
      method: 'GET',
      success: (res) => {
        this.setData({ phoneAvailable: res.data.available });
        if (!res.data.available) {
          wx.showToast({ title: '手机号已被注册', icon: 'none' });
        }
      }
    });
  },

  // 注册
  handleRegister() {
    const { username, password, userPhone, usernameAvailable, phoneAvailable } = this.data;
    if (!username || !password || !userPhone) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return;
    }
    if (usernameAvailable !== true) {
      wx.showToast({ title: '请检查用户名', icon: 'none' });
      return;
    }
    if (phoneAvailable !== true) {
      wx.showToast({ title: '请检查手机号', icon: 'none' });
      return;
    }
    this.setData({ loading: true });
    wx.request({
      url: 'http://localhost:8080/api/users/register',
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: { username, password, userPhone },
      success: (res) => {
        if (res.data.success) {
          wx.showToast({ title: '注册成功', icon: 'success' });
          wx.navigateBack();
        } else {
          wx.showToast({ title: res.data.message || '注册失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络请求失败', icon: 'none' });
      },
      complete: () => this.setData({ loading: false })
    });
  },

  // 登录接口调用
  handleLogin() {
    const { username, password } = this.data;
    if (!username || !password) {
      wx.showToast({ title: '请输入完整信息', icon: 'none' });
      return;
    }

    this.setData({ loading: true });

    wx.request({
      url: 'http://localhost:8080/api/users/login', // 改为你的后端接口
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: { username, password },
      success: (res) => {
        if (res.data.success) {
          wx.setStorageSync('userInfo', res.data.user);
          wx.showToast({ title: '登录成功', icon: 'success' });
          wx.navigateBack();
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