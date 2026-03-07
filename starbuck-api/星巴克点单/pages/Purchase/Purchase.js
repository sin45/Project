// pages/Purchase/Purchase.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    currentCategory: 1, // 当前选中的分类ID
    scrollTop: 0,
    rightScrollTop: 0, // 右侧列表滚动位置，用于点击左侧时联动
    categoryPositions: [], // 存储分类元素位置信息
    isScrolling: false,     // 滚动状态标志
    showCart: false, // 是否显示购物车弹窗
    totalQuantity: 0, // 购物车总数量
    totalPrice: 0, // 购物车总价
    originalTotalPrice: 0, // 原价总价（用于显示折扣)
    categories: [],
    allFoods:[],
    cartItems: [],
    checkoutBar: {
      total: 0,
      price: '0.00',
      active: false,
      statusText: '未选择'
    }
  },

   /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.initCartData();
    // 拉取商品数据并组装分类
    const app = getApp();
    wx.request({
      url: app.apiUrl('/api/products'),
      method: 'GET',
      success: (res) => {
        if (res.statusCode === 200 && Array.isArray(res.data)) {
          // 按 category 分组
          const group = {};
          res.data.forEach(prod => {
            const cat = prod.category || '默认分类';
            if (!group[cat]) group[cat] = [];
            group[cat].push({
              id: prod.productId,
              categoryId: cat, // 直接用 category 字段
              name: prod.productName,
              description: prod.description,
              price: prod.price,
              image: prod.imageUrl,
              quantity: 0
            });
          });
          // 组装 categories
          const categories = Object.keys(group).map(cat => ({
            id: cat, // 直接用 category 字段
            name: cat,
            count: 0,
            foods: group[cat]
          }));
          this.setData({
            categories,
            currentCategory: categories.length > 0 ? categories[0].id : '',
          }, () => {
            this.getCategoryPositions();
          });
        }
      },
      fail: (err) => {
        console.error('商品接口请求失败', err);
      }
    });
  },

  // 初始化购物车数据
  initCartData() {
    const app = getApp();
    const cartItems = app.getCartItems();
    this.setData({ cartItems }, () => {
      this.calculateCartTotal();
    });
  },

  // 跳转到商品详情页
  goToProduct(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/product/product?id=${id}`,
    });
  },

  // 根据ID查找商品
  findProductById(id) {
    for (const category of this.data.categories) {
      const product = category.foods.find(food => food.id == id);
      if (product) return product;
    }
    return null;
  },

  // 获取右侧每个分类块在滚动容器中的相对位置（用于 scroll-top 和滚动联动）
  getCategoryPositions() {
    const query = wx.createSelectorQuery().in(this);
    query.selectAll('.category-section').boundingClientRect();
    query.exec(res => {
      const rects = res[0] || [];
      if (!rects.length) return;

      // 以第一个分类块的顶部为基准，计算各块在滚动内容中的 scrollTop 偏移
      const baseTop = rects[0].top;
      const positions = rects.map((rect, index) => ({
        id: this.data.categories[index].id,
        top: Math.round(rect.top - baseTop),
        bottom: Math.round(rect.bottom - baseTop)
      }));

      this.setData({ categoryPositions: positions });
    });
  },

  // 切换分类
  switchCategory(e) {
    const categoryId = e.currentTarget.dataset.id;
    const { categoryPositions, categories } = this.data;

    // 用 scroll-top 联动右侧滚动（比 scroll-into-view 更可靠）
    let targetScrollTop = 0;
    const index = categories.findIndex(c => c.id == categoryId);
    if (index >= 0 && categoryPositions[index]) {
      targetScrollTop = categoryPositions[index].top;
    }

    this.setData({
      currentCategory: categoryId,
      rightScrollTop: targetScrollTop,
      isScrolling: true
    });

    setTimeout(() => {
      this.setData({ isScrolling: false });
    }, 400);
  },

  // 滚动事件处理
  onScroll(e) {
    if (this.data.isScrolling || !this.data.categoryPositions.length) return;
    
    clearTimeout(this.scrollTimer);
    this.scrollTimer = setTimeout(() => {
      this.updateActiveCategory(e.detail.scrollTop);
    }, 100);
  },

  // 更新当前分类
  updateActiveCategory(scrollTop) {
    const { categoryPositions, currentCategory } = this.data;
    if (!categoryPositions.length) return;

    // 根据 scrollTop 找到当前可视区域最靠上的分类块
    let newCategory = currentCategory;
    for (let i = categoryPositions.length - 1; i >= 0; i--) {
      if (scrollTop >= categoryPositions[i].top - 10) {
        newCategory = categoryPositions[i].id;
        break;
      }
    }

    if (newCategory !== currentCategory) {
      this.setData({ currentCategory: newCategory });
    }
  },

  // 计算购物车总数和总价
  calculateCartTotal() {
    const app = getApp();
    const cartTotal = app.calculateCartTotal();
    
    // 确保转换为数字类型
    const totalPrice = Number(cartTotal.totalPrice) || 0;
    const totalQuantity = Number(cartTotal.totalQuantity) || 0;
    
    this.setData({
      totalQuantity,
      totalPrice,
      originalTotalPrice: totalPrice
    });
    
    this.updateCheckoutBar();
  },

  // 更新结算栏状态
  updateCheckoutBar() {
    // 确保 totalPrice 是数字类型
    const totalPrice = Number(this.data.totalPrice) || 0;
    const totalQuantity = Number(this.data.totalQuantity) || 0;
    const active = totalQuantity > 0;
    
    this.setData({
      'checkoutBar.total': totalQuantity,
      'checkoutBar.price': totalPrice.toFixed(2), // 现在可以安全调用 toFixed()
      'checkoutBar.active': active,
      'checkoutBar.statusText': active ? '去结算' : '未选择'
    });
  },
  // 结算功能
  checkout() {
    if (!this.data.checkoutBar.active) {
      wx.showToast({ title: '请先选择商品', icon: 'none' });
      return;
    }
    
    wx.navigateTo({
      url: `/pages/Checkout/Checkout?total=${this.data.totalPrice.toFixed(2)}`,
      fail: (err) => {
        console.error('跳转失败:', err);
        wx.showToast({ title: '跳转失败，请重试', icon: 'none' });
      }
    });
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
    // 确保在页面渲染后获取分类位置（API 失败时使用默认数据也需要）
    setTimeout(() => this.getCategoryPositions(), 100);
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    this.initCartData();
  },
  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  }


})