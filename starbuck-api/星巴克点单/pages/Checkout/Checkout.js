// pages/Checkout/Checkout.js
Page({
  data: {
    agreed: false, // 是否同意协议
    pickupType: 'immediate', // 取单类型
    scheduledTime: '', // 预约时间
    showTimePicker: false, // 是否显示时间选择器
    dates: ['今天', '明天'], // 可选日期
    hours: Array.from({length: 24}, (_, i) => i), // 小时选项
    minutes: Array.from({length: 12}, (_, i) => i *5), // 分钟选项
    timeIndex: [0, 9, 0], // 时间选择器默认选中
    cartItems: [], // 购物车商品列表
    totalPrice: '0.00', // 订单总价
    selectedStore: null, // 选择的门店
    storeAddress: '', // 门店地址
    currentTime: '', // 当前时间
    orderType: 'pickup', // 取餐方式：pickup 自提，delivery 外送
    address: '', // 自提门店地址
    selectedAddress: '', // 配送地址对象
    remark: ''//备注信息
  },

  onLoad(options) {
    // 解析来自 Purchase 页面的参数
    const {
      total,
      orderType,
      address,
      selectedAddress
    } = options || {};

    let decodedAddress = address ? decodeURIComponent(address) : '';
    let parsedSelectedAddress = null;

    if (selectedAddress) {
      try {
        const jsonStr = decodeURIComponent(selectedAddress);
        parsedSelectedAddress = JSON.parse(jsonStr);
      } catch (e) {
        console.warn('selectedAddress 解析失败', e);
      }
    }

    // 计算展示用地址
    const finalOrderType = orderType || 'pickup';
    let displayAddress = '';
    if (finalOrderType === 'pickup') {
      displayAddress = decodedAddress || '';
    } else if (finalOrderType === 'delivery' && parsedSelectedAddress) {
      displayAddress =
        parsedSelectedAddress.fullAddress ||
        parsedSelectedAddress.address ||
        parsedSelectedAddress.detailAddress ||
        '';
    }

    this.setData({
      totalPrice: total || this.data.totalPrice,
      orderType: finalOrderType,
      address: decodedAddress,
      selectedAddress: parsedSelectedAddress,
      displayAddress
    });

    this.initCartData();
    this.updateCurrentTime();
    this.initDefaultTime();
  },

  initCartData() {
    const app = getApp();
    const cartItems = app.getCartItems();
    const cartTotal = app.calculateCartTotal();
    
    const formattedCartItems = cartItems.map(item => ({
      ...item,
      id: item.productId || item.id,
      name: item.productName || item.name,
      price: parseFloat(item.price).toFixed(2),
      imageUrl: item.imageUrl,
      selectedSize: item.selectedSize || '标准',
      selectedTemp: item.selectedTemp || '标准',
      selectedFlavor: item.selectedFlavor || '标准',
      quantity: item.quantity || 1
    }));
    
    this.setData({
      cartItems: formattedCartItems,
      totalPrice: cartTotal.totalPrice
    });
  },

  //自提改外送
  changeToDelivery() {
    if(this.data.orderType=='pickup'){
      this.setData({
        orderType: 'delivery'
      });
    }
    if(this.data.selectedAddress==null || this.data.selectedAddress==''){
      this.getDefaultAddress();
    }
  },

  //外送改自提
  changeToPickup() {
    if(this.data.orderType=='delivery'){
      this.setData({
        orderType: 'pickup'
      });
    }
  },

  /**
   * 获取默认的配送地址
   */
  getDefaultAddress() {
    const userInfo = wx.getStorageSync('userInfo');
    const openId = userInfo ? userInfo.wxOpenid : '';
    if (!openId) {
      return;
    }

    const app = getApp();
    wx.request({
      url: app.apiUrl('/api/deliveryAddress/getDefaultAddress'),
      method: 'POST',
      header: {
        'content-type': 'application/json'
      },
      data: { openId },
      success: (res) => {
        if (res.data) {
          this.setData({
            selectedAddress: res.data
          });
        }else{
          wx.navigateTo({
            url: '/pages/addressManage/addressManage?ifPurchase=true'
          });
          wx.showToast({ title: '请添加配送地址', icon: 'none' });
        }
      }
    });
  },

   /**
   * 选择配送地址
   */
   changeAddress(){
    wx.navigateTo({
      url: '/pages/addressManage/addressManage?ifPurchase=true'
    });
  },

  // 修改点1：重构减少数量方法
  decreaseQuantity(e) {
    console.log('触发减少数量', e);
    const app = getApp();
    const id = e.currentTarget.dataset.id;
    const cartItems = this.data.cartItems;
    const item = cartItems.find(item => item.id === id);
    
    if (item && item.quantity > 1) {
      // 更新全局购物车
      app.updateCartItem(id, item.quantity - 1);
      
      // 更新本地数据并重新计算总价
      this.initCartData();
    } else {
      wx.showToast({
        title: '至少选择一件',
        icon: 'none'
      });
    }
  },

  // 修改点2：重构增加数量方法
  increaseQuantity(e) {
    const app = getApp();
    const id = e.currentTarget.dataset.id;
    const cartItems = this.data.cartItems;
    const item = cartItems.find(item => item.id === id);
    
    if (item) {
      // 更新全局购物车
      app.updateCartItem(id, item.quantity + 1);
      
      // 更新本地数据并重新计算总价
      this.initCartData();
    }
  },

  // 以下其他方法保持不变...
  initDefaultTime() {
    const now = new Date();
    now.setMinutes(now.getMinutes() + 30);
    this.setData({
      timeIndex: [
        0,
        now.getHours(),
        Math.ceil(now.getMinutes() / 5) * 5 / 5
      ]
    });
  },

  updateCurrentTime() {
    const now = new Date();
    this.setData({
      currentTime: `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    });
  },

  onAddressInput(e) {
    this.setData({ storeAddress: e.detail.value });
  },

  searchStore() {
    if (!this.data.storeAddress.trim()) {
      wx.showToast({ title: '请输入地址', icon: 'none' });
      return;
    }
    
    wx.showLoading({ title: '搜索中...' });
    
    setTimeout(() => {
      wx.hideLoading();
      this.setData({
        selectedStore: {
          name: `${this.data.storeAddress}店`,
          address: `${this.data.storeAddress}路123号`
        }
      });
    }, 1000);
  },

  // setPickupType(e) {
  //   const type = e.currentTarget.dataset.type;
  //   this.setData({
  //     pickupType: type,
  //     scheduledTime: type === 'immediate' ? '' : this.data.scheduledTime
  //   });
  // },

  // toggleTimePicker() {
  //   this.setData({ showTimePicker: !this.data.showTimePicker });
  // },

  // timeChange(e) {
  //   const value = e.detail.value;
  //   this.setData({
  //     timeIndex: [
  //       Math.min(value[0], this.data.dates.length - 1),
  //       Math.min(value[1], this.data.hours.length - 1),
  //       Math.min(value[2], this.data.minutes.length - 1)
  //     ]
  //   });
  // },

  // confirmTime() {
  //   const { dates, hours, minutes, timeIndex } = this.data;
    
  //   const selectedDate = dates[timeIndex[0]]?.match(/今天|明天/)?.[0] || '今天';
  //   const hourValue = hours[timeIndex[1]] ?? 0;
  //   const minuteValue = minutes[timeIndex[2]] ?? 0;
    
  //   const selectedHour = hourValue.toString().padStart(2, '0');
  //   const selectedMinute = (minuteValue ).toString().padStart(2, '0');
    
  //   const now = new Date();
  //   const selectedTime = new Date();
    
  //   if (selectedDate === '明天') {
  //     selectedTime.setDate(selectedTime.getDate() + 1);
  //   }
    
  //   selectedTime.setHours(parseInt(selectedHour));
  //   selectedTime.setMinutes(parseInt(selectedMinute));
    
  //   if (selectedTime < now) {
  //     wx.showToast({ title: '不能选择过去的时间', icon: 'none' });
  //     return;
  //   }

  //   this.setData({
  //     scheduledTime: `${selectedDate} ${selectedHour}:${selectedMinute}`,
  //     showTimePicker: false,
  //     pickupType: 'schedule'
  //   });
  // },

  /**
   * 支付
   */
  confirmPayment() {
    const _this = this
    const userInfo = wx.getStorageSync('userInfo');
    const openid = userInfo && (userInfo.wx_openid || userInfo.wxOpenid);
    const app = getApp();
    //调用后台发起预支付
    wx.request({
      url: app.apiUrl('/api/wxPay/unifiedOrder'),
      method: 'POST',
      data: {
        openId: openid,
        amount: Math.round(totalPrice),
        description: '小程序下单支付'
      },
      header: { 'Content-Type': 'application/x-www-form-urlencoded' },
      success: (res) => {
        const data = res.data
        if (data.code !== 0) {
          wx.showToast({ title: data.msg || '下单失败', icon: 'none' })
          return
        }
        //预支付成功后 - 发起微信支付
        const payParams = data.data
        // payParams 内包含 timeStamp, nonceStr, package, signType, paySign
        wx.requestPayment({
          timeStamp: payParams.timeStamp,
          nonceStr: payParams.nonceStr,
          package: payParams.package,
          signType: payParams.signType,
          paySign: payParams.paySign,
          success() {
            wx.showToast({ title: '支付成功', icon: 'success' })
            //支付成功后创建订单
            _this.createOrder();
          },
          fail(err) {
            console.error('支付失败', err)
            wx.showToast({ title: '支付取消或失败', icon: 'none' })
          }
        })
      },
      fail: () => {
        wx.showToast({ title: '支付请求失败', icon: 'none' });
      }
    });
  },

  /**
   * 创建订单
   */
  createOrder() {

    const userInfo = wx.getStorageSync('userInfo');
    const userId = userInfo && (userInfo.userId || userInfo.User_id);

    // 组装订单数据
    const orderData = {
      userId: userId,
      storeId: this.data.selectedStore ? this.data.selectedStore.id : 1,
      pickupTime: new Date().toISOString(), // 可根据实际需求调整
      orderDetails: this.data.cartItems.map(item => ({
        productId: item.id,
        productName: item.name,
        productImage: item.imageUrl,
        quantity: item.quantity,
        unitPrice: item.price,
        subtotal: (item.price * item.quantity).toFixed(2),
        customization: [item.selectedSize, item.selectedTemp, item.selectedFlavor].join('/')
      }))
    };
    // 调用后端创建订单
    wx.request({
      url: app.apiUrl('/api/orders'),
      method: 'POST',
      data: orderData,
      header: { 'Content-Type': 'application/json' },
      success: (orderRes) => {
        // 清空购物车
        if (typeof app.clearCart === 'function') {
          app.clearCart();
        } else {
          wx.removeStorageSync('cartItems');
        }
        // 设置刷新用户信息标志
        wx.setStorageSync('refreshUserInfo', true);
        // 跳转订单
        setTimeout(() => {
          wx.switchTab({
            url: '/pages/DingDan/DingDan'
          });
        }, 800);
      },
      fail: () => {
        wx.showToast({ title: '订单保存失败', icon: 'none' });
      }
    });
  },

  toggleAgreement() {
    this.setData({ agreed: !this.data.agreed });
  },

  onShow() {
    this.initCartData();
    this.updateCurrentTime();
  }
});