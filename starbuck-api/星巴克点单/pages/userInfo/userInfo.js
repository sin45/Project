Page({
  data: {
    avatarUrl: '',
    nickname: '',
    accountMask: '178******53',
    gender: '', // 0 未选 1 男 2 女
    genderText: ''
  },

  onLoad() {
    // 可从本地存储或接口拉取已保存信息
    const user = wx.getStorageSync('profileEdit') || {};
    this.setData({
      avatarUrl: user.avatarUrl || '',
      nickname: user.nickname || '',
      accountMask: user.accountMask || '178******53',
      gender: user.gender != null ? user.gender : '',
      genderText: user.genderText || ''
    });
  },

  onChooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      sizeType: ['compressed'],
      success: (res) => {
        const tempPath = res.tempFiles[0].tempFilePath;
        this.setData({ avatarUrl: tempPath });
      }
    });
  },

  onNicknameInput(e) {
    const v = (e.detail.value || '').replace(/[^\w\u4e00-\u9fa5]/g, '');
    this.setData({ nickname: v });
  },

  onChooseGender() {
    wx.showActionSheet({
      itemList: ['男', '女'],
      success: (res) => {
        const list = ['', '男', '女'];
        this.setData({
          gender: res.tapIndex + 1,
          genderText: list[res.tapIndex + 1]
        });
      }
    });
  },

  onSave() {
    const { nickname, gender, genderText, avatarUrl, accountMask } = this.data;
    wx.setStorageSync('profileEdit', {
      avatarUrl,
      nickname,
      accountMask,
      gender,
      genderText
    });
    wx.showToast({ title: '保存成功', icon: 'success' });
    setTimeout(() => wx.navigateBack(), 1500);
  }
});
