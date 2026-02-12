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
    currentTime: '' // 当前时间
  },

  onLoad(options) {
    this.initCartData();
    this.updateCurrentTime();
    this.initDefaultTime();
    if (options.total) {
      this.setData({
        totalPrice: options.total
      });
    }
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

  setPickupType(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({
      pickupType: type,
      scheduledTime: type === 'immediate' ? '' : this.data.scheduledTime
    });
  },

  toggleTimePicker() {
    this.setData({ showTimePicker: !this.data.showTimePicker });
  },

  timeChange(e) {
    const value = e.detail.value;
    this.setData({
      timeIndex: [
        Math.min(value[0], this.data.dates.length - 1),
        Math.min(value[1], this.data.hours.length - 1),
        Math.min(value[2], this.data.minutes.length - 1)
      ]
    });
  },

  confirmTime() {
    const { dates, hours, minutes, timeIndex } = this.data;
    
    const selectedDate = dates[timeIndex[0]]?.match(/今天|明天/)?.[0] || '今天';
    const hourValue = hours[timeIndex[1]] ?? 0;
    const minuteValue = minutes[timeIndex[2]] ?? 0;
    
    const selectedHour = hourValue.toString().padStart(2, '0');
    const selectedMinute = (minuteValue ).toString().padStart(2, '0');
    
    const now = new Date();
    const selectedTime = new Date();
    
    if (selectedDate === '明天') {
      selectedTime.setDate(selectedTime.getDate() + 1);
    }
    
    selectedTime.setHours(parseInt(selectedHour));
    selectedTime.setMinutes(parseInt(selectedMinute));
    
    if (selectedTime < now) {
      wx.showToast({ title: '不能选择过去的时间', icon: 'none' });
      return;
    }

    this.setData({
      scheduledTime: `${selectedDate} ${selectedHour}:${selectedMinute}`,
      showTimePicker: false,
      pickupType: 'schedule'
    });
  },

  confirmPayment() {
    const userInfo = wx.getStorageSync('userInfo');
    const userId = userInfo && (userInfo.user_id || userInfo.userId);
    if (!userInfo || !userId) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const totalPrice = parseFloat(this.data.totalPrice);
    if (userInfo.money < totalPrice) {
      wx.showToast({ title: '余额不足，请向柜员充值', icon: 'none' });
      return;
    }
    wx.request({
      url: `http://localhost:8080/api/balance/payment/${userId}`,
      method: 'POST',
      data: {
        amount: Math.round(totalPrice),
        remarks: '小程序下单支付'
      },
      header: { 'Content-Type': 'application/x-www-form-urlencoded' },
      success: (res) => {
        if (res.data && res.data.success !== false) {
          userInfo.money -= totalPrice;
          wx.setStorageSync('userInfo', userInfo);
          wx.showToast({ title: '支付成功', icon: 'success' });

          // 组装订单数据
          const orderData = {
            userId,
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
            url: 'http://localhost:8080/api/orders',
            method: 'POST',
            data: orderData,
            header: { 'Content-Type': 'application/json' },
            success: (orderRes) => {
              // 清空购物车
              const app = getApp();
              if (typeof app.clearCart === 'function') {
                app.clearCart();
              } else {
                wx.removeStorageSync('cartItems');
              }
              // 设置刷新用户信息标志
              wx.setStorageSync('refreshUserInfo', true);
              // 跳转首页
              setTimeout(() => {
                wx.switchTab({
                  url: '/pages/ShouYe/ShouYe'
                });
              }, 800);
            },
            fail: () => {
              wx.showToast({ title: '订单保存失败', icon: 'none' });
            }
          });
        } else {
          wx.showToast({ title: res.data.message || '支付失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '支付请求失败', icon: 'none' });
      }
    });
  },

  /**
   * 新的微信支付
   */
  newPay() {
    const that = this
    const userInfo = wx.getStorageSync('userInfo');
    wx.request({
      url: 'https://your-domain.com/api/pay/unifiedOrder',
      method: 'POST',
      header: {
        'content-type': 'application/json'
      },
      data: {
        openid: userInfo.wx_openid,       
        amount: 1,                   // 金额（单位：分）
        description: '星巴克饮品一杯' // 商品描述
      },
      success(res) {
        const data = res.data
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
            // TODO 支付成功后的业务处理（例如刷新订单状态）
          },
          fail(err) {
            console.error('支付失败', err)
            wx.showToast({ title: '支付取消或失败', icon: 'none' })
          }
        })
      },
      fail(err) {
        console.error('统一下单接口失败', err)
        wx.showToast({ title: '网络异常', icon: 'none' })
      }
    })

  },

  toggleAgreement() {
    this.setData({ agreed: !this.data.agreed });
  },

  onShow() {
    this.initCartData();
    this.updateCurrentTime();
  }
});