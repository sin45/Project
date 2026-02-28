// pages/ProductDetail/ProductDetail.js
const app = getApp();

Page({
  /**
   * 页面的初始数据
   */
  data: {
    product: null,
    totalPrice: '0.00',
    quantity: 1,
    espressoQuantity: 1,
    selectedSize: 'large',
    selectedTemp: 'ice',
    selectedEspresso: 'classic',
    selectedFlavor: 'berry',
    showStickyHeader: false,
    scrollThreshold: 400,
    statusBarHeight: 0,
    navBarHeight: 0,
    
    // 杯型选项数据
    sizeOptions: [
      {
        id: 'medium',
        name: '中杯',
        volume: '355ml',
        icon: '/pages/images/中杯.jpg',
        selectedIcon: '/pages/images/选中中杯.jpg'
      },
      {
        id: 'large',
        name: '大杯',
        volume: '473ml',
        icon: '/pages/images/大杯.jpg',
        selectedIcon: '/pages/images/选中大杯.jpg'
      },
      {
        id: 'xlarge',
        name: '超大杯',
        volume: '592ml',
        icon: '/pages/images/超大杯.jpg',
        selectedIcon: '/pages/images/选中超大杯.jpg'
      }
    ],
    
    // 温度选项
    tempOptions: ['热', '微热', '去冰','少冰','标准冰','多冰'],
    
    // 浓缩咖啡选项
    espressoOptions: [
      '经典浓缩（深烘）',
      '金烘浓缩（浅烘）',
      '低因浓缩（深烘）'
    ],
  },

  onLoad(options) {
    const sysInfo = wx.getSystemInfoSync();
    const id = options.id;
    if (!id) {
      wx.showToast({ title: '缺少商品ID', icon: 'none' });
      return;
    }
    // 拉取商品详情
    wx.request({
      url: `http://localhost:8080/api/products/${id}`,
      method: 'GET',
      success: (res) => {
        if (res.statusCode === 200 && res.data) {
          this.setData({
            product: res.data,
            totalPrice: res.data.price ? Number(res.data.price).toFixed(2) : '0.00',
            quantity: 1,
            statusBarHeight: sysInfo.statusBarHeight,
            navBarHeight: sysInfo.statusBarHeight + 44
          });
        } else {
          wx.showToast({ title: '商品不存在', icon: 'none' });
        }
      },
      fail: (err) => {
        wx.showToast({ title: '商品接口请求失败', icon: 'none' });
        console.error('商品详情接口请求失败', err);
      }
    });
  },

  onPageScroll(e) {
    const showHeader = e.scrollTop > this.data.scrollThreshold;
    if (showHeader !== this.data.showStickyHeader) {
      this.setData({ showStickyHeader: showHeader });
    }
  },

  // 数量减少
  decreaseQuantity() {
    if (this.data.quantity > 1) {
      this.setData({
        quantity: this.data.quantity - 1
      }, () => {
        this.calculateTotalPrice();
      });
    } else {
      wx.showToast({
        title: '至少选择一件',
        icon: 'none'
      });
    }
  },

  // 数量增加
  increaseQuantity() {
    this.setData({
      quantity: this.data.quantity + 1
    }, () => {
      this.calculateTotalPrice();
    });
  },
  // 浓缩数量减少
  decreaseEspressoQuantity() {
    if (this.data.espressoQuantity > 1) {
      this.setData({
        espressoQuantity: this.data.espressoQuantity - 1
      }, () => {
        this.calculateTotalPrice();
      });
    } else {
      wx.showToast({
        title: '至少选择一件',
        icon: 'none'
      });
    }
  },

  // 浓缩数量增加
  increaseEspressoQuantity() {
    this.setData({
      espressoQuantity: this.data.espressoQuantity + 1
    }, () => {
      this.calculateTotalPrice();
    });
  },

  // 添加到购物车
  addToCart() {
    if (!this.data.product) {
      wx.showToast({
        title: '商品信息错误',
        icon: 'error'
      });
      return;
    }
    const cartItem = {
      ...this.data.product,
      quantity: this.data.quantity,
      selectedSize: this.data.selectedSize,
      selectedTemp: this.data.selectedTemp,
      selectedEspresso: this.data.selectedEspresso,
      selectedFlavor: this.data.selectedFlavor,
      totalPrice: this.data.totalPrice,
      price: this.data.product.price
    };
    const count = app.addToCart(cartItem);
    wx.showToast({
      title: `已添加到购物车 (${count}件)`,
      icon: 'success',
      duration: 2000
    });
    this.goBack();
  },

  // 计算总价
  calculateTotalPrice() {
    if (!this.data.product) return;
    const totalPrice = Number(this.data.product.price || 0) * this.data.quantity + (this.data.espressoQuantity - 1) * 6;
    this.setData({
      totalPrice: totalPrice.toFixed(2)
    });
  },

  // 重置定制选项
  resetCustomization() {
    this.setData({
      quantity: 1,
      selectedSize: 'large',
      selectedTemp: 'ice',
      selectedEspresso: 'classic',
      selectedFlavor: 'berry'
    }, () => {
      this.calculateTotalPrice();
    });
  },
  
  // 选择器方法
  selectSize(e) {
    this.setData({ selectedSize: e.currentTarget.dataset.size });
  },

  selectTemperature(e) {
    this.setData({ selectedTemp: e.currentTarget.dataset.temp });
  },

  selectEspresso(e) {
    this.setData({ selectedEspresso: e.currentTarget.dataset.espresso });
  },

  selectFlavor(e) {
    this.setData({ selectedFlavor: e.currentTarget.dataset.flavor });
  },

  // 返回上一页
  goBack() {
    setTimeout(() => {
      wx.navigateTo({
        url: '/pages/Purchase/Purchase',
      });
    }, 1000);
  },

  switchCategory(e) {
    const categoryId = e.currentTarget.dataset.id;
    this.setData({
      currentCategory: categoryId,
      isScrolling: true
    });

    wx.pageScrollTo({
      selector: `#category-${categoryId}`,
      duration: 300,
      complete: () => {
        setTimeout(() => {
          this.setData({ isScrolling: false });
        }, 500);
      }
    });
  }
});