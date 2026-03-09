Page({
  data: {
    id: '',
    openid: '',
    receiver: '',
    phone: '',
    address1: '',
    address2: '',
    default_flag: false,
    if_change: false
  },

  onLoad(options) {
    if (options.data) {
      
      let jsonString = options.data;
      jsonString = decodeURIComponent(jsonString);
      let jsonData = JSON.parse(jsonString);
      this.setData(jsonData);
      
    }
  },

  onNameInput(e) {
    this.setData({ 
      receiver: (e.detail.value || '').trim(),
      if_change: true
    });
  },

  onPhoneInput(e) {
    this.setData({ 
      phone: (e.detail.value || '').replace(/\D/g, ''),
      if_change: true
    });
  },

  onDetailInput(e) {
    this.setData({
       address2: (e.detail.value || '').trim(),
       if_change: true
    });
  },

  onDefaultChange(e) {
    this.setData({ 
      default_flag: e.detail.value,
      if_change: true 
    });
  },

  onAuthPhone() {
    // 需要配合 button open-type="getPhoneNumber" 获取手机号，此处仅提示
    wx.showModal({
      title: '授权手机号',
      content: '请在下一版中使用「获取手机号」按钮完成授权',
      showCancel: false
    });
  },

  onConfirm() {
    const { receiver, phone, address1, address2 } = this.data;
    if (!receiver) {
      wx.showToast({ title: '请填写姓名', icon: 'none' });
      return;
    }
    if (!phone) {
      wx.showToast({ title: '请填写手机号', icon: 'none' });
      return;
    }
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请填写正确手机号', icon: 'none' });
      return;
    }
    if (!address1) {
      wx.showToast({ title: '请选择收货地址地区', icon: 'none' });
      return;
    }
    if (!address2) {
      wx.showToast({ title: '请填写详细地址', icon: 'none' });
      return;
    }

    this.saveAddress();
  },

  /**
   * 保存地址
   */
  saveAddress() {

    const app = getApp();
    const userInfo = wx.getStorageSync('userInfo');
    const openId = userInfo ? userInfo.wxOpenid : '';

    const { id, receiver, phone, address1, address2, default_flag } = this.data;

    wx.request({
      url: app.apiUrl('/api/deliveryAddress/updateAddress'),
      method: 'POST',
      header: {
        'content-type': 'application/json'
      },
      data: {
        id: id || null,
        openId: openId,
        receiver: receiver,
        phone: phone,
        address1: address1,
        address2: address2,
        default_flag: default_flag ? '1' : '0'
      },
      success: () => {
        wx.showToast({
          title: id ? '保存成功' : '添加成功',
          icon: 'success'
        });
        setTimeout(() => wx.navigateBack(), 1500);
      },
      fail: () => {
        wx.showToast({
          title: '保存失败，请稍后重试',
          icon: 'none'
        });
      }
    });
  },

  /**
   * 地图选择地址
   */
  selectAddress() {
    wx.navigateTo({ url: '/pages/addressSelect/addressSelect' });
  }
});
