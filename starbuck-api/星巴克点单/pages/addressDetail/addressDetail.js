Page({
  data: {
    name: '',
    phone: '',
    gender: 2, // 1 先生 2 女士
    regionText: '',
    detail: '',
    isDefault: false,
    id: null // 编辑时传入
  },

  onLoad(options) {
    if (options.id) {
      const list = wx.getStorageSync('addressList') || [];
      const item = list.find((a) => String(a.id) === options.id);
      if (item) {
        this.setData({
          id: item.id,
          name: item.name || '',
          phone: item.phone || '',
          gender: item.gender || 2,
          regionText: item.regionText || '',
          detail: item.detail || '',
          isDefault: !!item.isDefault
        });
      }
    }
  },

  onNameInput(e) {
    this.setData({ name: (e.detail.value || '').trim() });
  },

  onPhoneInput(e) {
    this.setData({ phone: (e.detail.value || '').replace(/\D/g, '') });
  },

  onGenderTap(e) {
    const value = parseInt(e.currentTarget.dataset.value, 10);
    this.setData({ gender: value });
  },

  onDetailInput(e) {
    this.setData({ detail: (e.detail.value || '').trim() });
  },

  onDefaultChange(e) {
    this.setData({ isDefault: e.detail.value });
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
    const { name, phone, gender, regionText, detail, isDefault, id } = this.data;
    if (!name) {
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
    if (!regionText) {
      wx.showToast({ title: '请选择收货地址地区', icon: 'none' });
      return;
    }
    if (!detail) {
      wx.showToast({ title: '请填写详细地址', icon: 'none' });
      return;
    }

    const list = wx.getStorageSync('addressList') || [];
    const payload = {
      id: id || Date.now(),
      name,
      phone,
      gender,
      regionText,
      detail,
      isDefault
    };
    const idx = list.findIndex((a) => String(a.id) === String(id));
    if (idx >= 0) {
      list[idx] = payload;
    } else {
      if (isDefault) {
        list.forEach((a) => (a.isDefault = false));
      }
      list.push(payload);
    }
    if (isDefault) {
      list.forEach((a) => (a.isDefault = a.id === payload.id));
    }
    wx.setStorageSync('addressList', list);
    wx.showToast({ title: id ? '保存成功' : '添加成功', icon: 'success' });
    setTimeout(() => wx.navigateBack(), 1500);
  },
  /**
   * 地图选择地址
   */
  selectAddress() {
    wx.navigateTo({ url: '/pages/addressSelect/addressSelect' });
  }
});
