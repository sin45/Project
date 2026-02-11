Page({
  data: {
    cityName: '威海市',
    keyword: '',
    currentIndex: -1,
    latitude: 0,
    longitude: 0,
    markers: [],
    addressList: [
      {
        id: 1,
        title: '水岸人家',
        desc: '金湖路与长江路交叉口西北方向380米左右'
      },
      {
        id: 2,
        title: '水岸人家43号楼',
        desc: '淮河路水岸人家'
      },
      {
        id: 3,
        title: '水岸人家40号楼',
        desc: '白沙滩镇银滩滨海旅游度假区水岸人家'
      },
      {
        id: 4,
        title: '水岸人家44号楼',
        desc: '示例地址文案，可自行替换'
      }
    ]
  },

  onLoad() {
    this.getLocationPermission();
    this.initLocation();
  },
  // 获取定位权限
  async getLocationPermission() {
    return new Promise((resolve) => {
      wx.getSetting({
        success: (res) => {
          if (!res.authSetting['scope.userLocation']) {
            // 用户未授权，弹出授权弹窗
            wx.authorize({
              scope: 'scope.userLocation',
              success: () => {
                // 用户同意授权，获取位置
                this.getCurrentLocation();
                resolve(true);
              },
              fail: () => {
                // 用户拒绝授权，提示用户前往设置开启
                wx.showModal({
                  title: '权限请求',
                  content: '需要获取您的地理位置才能使用地图功能，是否去设置中开启？',
                  success: (modalRes) => {
                    if (modalRes.confirm) {
                      wx.openSetting({
                        success: (settingRes) => {
                          if (settingRes.authSetting['scope.userLocation']) {
                            this.initLocation();
                            resolve(true);
                          } else {
                            resolve(false);
                          }
                        },
                        fail: () => resolve(false)
                      });
                    } else {
                      resolve(false);
                    }
                  }
                });
              }
            });
          } else {
            // 用户已授权，直接获取位置
            this.initLocation();
            resolve(true);
          }
        },
        fail: () => resolve(false)
      });
    });
  },

  initLocation() {
    wx.getLocation({
      type: 'gcj02',
      isHighAccuracy: true,
      success: (res) => {
        const { latitude, longitude } = res;
        this.setData({
          latitude,
          longitude,
          markers: [
            {
              id: 1,
              latitude,
              longitude,
              width: 32,
              height: 32
            }
          ]
        });
      },
      fail: () => {
        wx.showToast({
          title: '定位失败，请检查定位权限',
          icon: 'none'
        });
      }
    });
  },

  onKeywordInput(e) {
    this.setData({
      keyword: e.detail.value || ''
    });
  },

  onSearchConfirm() {
    const { keyword } = this.data;
    if (!keyword) return;
    // TODO：接入地图搜索接口
    wx.showToast({
      title: '搜索功能示例',
      icon: 'none'
    });
  },

  onCityTap() {
    // TODO：跳转城市选择页
    wx.showToast({
      title: '城市选择示例',
      icon: 'none'
    });
  },

  onSelectItem(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      currentIndex: index
    });
  },

  onConfirm() {
    const { currentIndex, addressList } = this.data;
    if (currentIndex === -1) {
      wx.showToast({
        title: '请选择一个地址',
        icon: 'none'
      });
      return;
    }

    const item = addressList[currentIndex];
    // 示例：把选中的地址存起来，或通过事件回传
    wx.setStorageSync('selectedAddress', item);

    wx.showToast({
      title: '已选择',
      icon: 'success'
    });

    setTimeout(() => {
      wx.navigateBack();
    }, 800);
  }
});

