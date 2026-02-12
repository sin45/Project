// pages/Purchase/Purchase.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
      currentCategory: 1, // 当前选中的分类ID
      scrollTop: 0,
      categoryPositions: [], // 存储分类元素位置信息
      isScrolling: false,     // 滚动状态标志
      showCart: false, // 是否显示购物车弹窗
      totalQuantity: 0, // 购物车总数量
      totalPrice: 0, // 购物车总价
      originalTotalPrice: 0, // 原价总价（用于显示折扣)
      categories: [
        {
          id: 1,
          name: "星级套餐",
          count: 0,
          foods: [
            {
              id: 101,
              categoryId: 1,
              name: "疯狂动物城系列",
              description: "双层椰芒布丁",
              price: 39.9,
              originalPrice: 48,
              discount: "8.3",
              image: "/pages/images/蓝调爆珠冰摇 .jpg",
              quantity: 0
            },
            {
              id: 102,
              categoryId: 1,
              name: "果香蜜香双重配",
              description: "shake一夏，星动全城",
              price: 35,
              image: "/pages/images/果香蜜香.jpg",
              quantity: 0
            }
          ]
        },
        {
          id: 2,
          name: "星巴克必点",
          count: 0,
          foods: [
            {
              id: 201,
              categoryId: 2,
              name: "星星淘梨冰摇茶",
              price: 26,
              originalPrice: 32,
              discount: "8.2",
              image: "/pages/images/桃梨冰摇.jpg",
              quantity: 0
            },
            {
              id: 202,
              categoryId: 2,
              name: "全橙胡闹冰摇茶",
              price: 26,
              image: "/pages/images/胡闹冰摇茶.jpg",
              quantity: 0
            },
            {
              id: 203,
              categoryId: 2,
              name: "仲夏蓝调爆珠冰摇茶",
              price: 30,
              image: "/pages/images/蓝调爆珠冰摇 .jpg",
              quantity: 0
            },
            {
              id: 204,
              categoryId: 2,
              name: "焦糖玛奇朵",
              price: 37,
              image: "/pages/images/商品7.jpg",
              quantity: 0
            },
            {
              id: 205,
              categoryId: 2,
              name: "燕麦拿铁",
              price: 33,
              image: "/pages/images/商品1.jpg",
              quantity: 0
            },
            {
              id: 206,
              categoryId: 2,
              name: "五黑芝士贝果",
              price: 19,
              image: "/pages/images/五黑贝果.jpg",
              quantity: 0
            },
            {
              id: 207,
              categoryId: 2,
              name: "绵小熊蛋糕",
              price: 36,
              image: "/pages/images/小熊蛋糕.jpg",
              quantity: 0
            },
          ]
        },
        {
          id: 3,
          name: "夏日心动推荐",
          count: 0,
          foods: [
            {
            id: 301,
            categoryId: 3,
            name: "冰摇红莓黑加仑茶",
            price: 26,
            image: "/pages/images/黑加仑冰摇.jpg",
            quantity: 0
          },
          {
            id: 302,
            categoryId: 3,
            name: "抹茶星冰乐",
            price: 32,
            image: "/pages/images/商品5.jpg",
            quantity: 0
          },
          {
            id: 303,
            categoryId: 3,
            name: "红茶拿铁",
            price: 29,
            image: "/pages/images/商品4.jpg",
            quantity: 0
          },
          {
            id: 304,
            categoryId: 3,
            name: "冰摇桃桃乌龙茶",
            price: 32,
            image: "/pages/images/黑加仑冰摇.jpg",
            quantity: 0
          },
          {
            id: 305,
            categoryId: 4,
            name: "摩卡可可碎片星冰乐",
            price: 36,
            image: "/pages/images/商品5.jpg",
            quantity: 0
          },
      ],
    },
    {
      id: 4,
      name: "本店特供",
      count: 0,
       foods: [
        {
          id: 401,
          categoryId: 4,
          name: "金烘馥芮白",
          description: "大杯/热/全脂牛奶",
          price: 38,
          image: "/pages/images/商品6.jpg",
          quantity: 0
        },
        {
          id: 402,
          categoryId: 4,
          name: "金烘焦糖玛奇朵",
          description: "大杯/热/全脂牛奶",
          price: 37,
          image: "/pages/images/商品7.jpg",
          quantity: 0
        },
       ]
      },
      {
       id: 5,
       name: "真味无糖",
       count: 0,
       foods: [
         {id: 501,
        categoryId: 5,
        name: "草莓风味拿铁",
        description: "不另外加糖/热/全脂牛奶",
        price: 36,
        image: "/pages/images/商品10.png",
        quantity: 0
        }
       ]
      },
      {
        id: 6,
        name: "巧克力及其他饮品",
        count: 0,
        foods: [
          {id: 601,
            categoryId: 6,
            name: "经典巧克力饮品",
            description: "不另外加糖/热/全脂牛奶",
            price: 33,
            image: "/pages/images/商品8.jpg",
            quantity: 0
            }
        ]
       },
       {
        id: 7,
        name: "经典咖啡",
        count: 0,
        foods: [
          {id: 701,
            categoryId: 7,
            name: "蓝莓轻气泡拿铁",
            description: "大杯/冰/全脂牛奶",
            price: 39,
            image: "/pages/images/蓝莓拿铁.jpg",
            quantity: 0
            },
            {id: 702,
              categoryId: 7,
              name: "甜橙轻气泡拿铁",
              description: "大杯/冰",
              price: 39,
              image: "/pages/images/甜橙拿铁.jpg",
              quantity: 0
              },
              {id: 703,
                categoryId: 7,
                name: "白桃气泡美式",
                description: "大杯/冰",
                price: 39,
                image: "/pages/images/黑加仑冰摇.jpg",
                quantity: 0
                },
          ]
        },
       {
        id: 8,
        name: "/浓/小杯",
        count: 0,
        foods: [
              {id: 801,
                categoryId: 8,
                name: "/浓/小杯拿铁",
                description: "小杯/热/牛奶",
                price: 29,
                image: "/pages/images/商品3.jpg",
                quantity: 0
          }
        ]
       },
       {
        id: 9,
        name: "星冰乐",
        count: 0,
        foods: []
       },
       {
        id: 10,
        name: "冰摇茶",
        count: 0,
        foods: []
       },
       {
        id: 11,
        name: "茶拿铁",
        count: 0,
        foods: []
       },
       {
        id: 12,
        name: "低因推荐",
        count: 0,
        foods: []
       },
       {
        id: 13,
        name: "巧克力及其他饮品",
        count: 0,
        foods: []
       },
       {
        id: 14,
        name: "烘焙&三明治",
        count: 0,
        foods: []
       },
       {
        id: 15,
        name: "蛋糕&星享小点",
        count: 0,
        foods: []
       },
       {
        id: 16,
        name: "星轻食&酸奶",
        count: 0,
        foods: []
       },
    ],
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
    wx.request({
      url: 'http://localhost:8080/api/products',
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

  // 获取分类元素位置信息
  getCategoryPositions() {
    const query = wx.createSelectorQuery();
    const positions = [];
    
    this.data.categories.forEach((category) => {
      query.select(`#category-${category.id}`).boundingClientRect();
    });
    
    query.exec((res) => {
      res.forEach((rect, index) => {
        if (rect) {
          positions.push({
            id: this.data.categories[index].id,
            top: rect.top,
            bottom: rect.bottom
          });
        }
      });
      this.setData({ categoryPositions: positions });
    });
  },

  // 切换分类
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
    const screenHeight = wx.getSystemInfoSync().windowHeight;
    const visibleCenter = scrollTop + (screenHeight - 100) / 2;
    
    let newCategory = currentCategory;
    for (const position of categoryPositions) {
      if (position.top <= visibleCenter && position.bottom >= visibleCenter) {
        newCategory = position.id;
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