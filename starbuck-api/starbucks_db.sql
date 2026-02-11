/*
 Navicat Premium Dump SQL

 Source Server         : zyq
 Source Server Type    : MySQL
 Source Server Version : 80300 (8.3.0)
 Source Host           : localhost:3306
 Source Schema         : starbucks_db

 Target Server Type    : MySQL
 Target Server Version : 80300 (8.3.0)
 File Encoding         : 65001

 Date: 03/07/2025 15:49:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for balance_log
-- ----------------------------
DROP TABLE IF EXISTS `balance_log`;
CREATE TABLE `balance_log`  (
  `log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` int NOT NULL COMMENT '关联用户ID',
  `change_amount` int NOT NULL COMMENT '变动金额（正数为增加，负数为减少）',
  `change_type` enum('RECHARGE','PAYMENT','REFUND','BONUS') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变动类型',
  `change_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变动时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `balance_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`User_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of balance_log
-- ----------------------------
INSERT INTO `balance_log` VALUES (1, 1, 1000, 'RECHARGE', '2025-06-28 15:16:03', '星礼卡充值');
INSERT INTO `balance_log` VALUES (2, 1, -35, 'PAYMENT', '2025-06-28 15:16:03', '购买美式咖啡');
INSERT INTO `balance_log` VALUES (3, 2, 2000, 'RECHARGE', '2025-06-28 15:16:03', '新春礼包充值');
INSERT INTO `balance_log` VALUES (4, 3, -120, 'PAYMENT', '2025-06-28 15:16:03', '购买两杯拿铁');

-- ----------------------------
-- Table structure for image_storage
-- ----------------------------
DROP TABLE IF EXISTS `image_storage`;
CREATE TABLE `image_storage`  (
  `image_id` int NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` int NOT NULL COMMENT '关联商品ID',
  `image_type` enum('MAIN','THUMBNAIL','DETAIL','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'MAIN' COMMENT '图片类型',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `is_primary` tinyint(1) NULL DEFAULT 0 COMMENT '是否主图',
  PRIMARY KEY (`image_id`) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `image_product_fk` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of image_storage
-- ----------------------------

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `order_id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `order_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号（唯一）',
  `total_amount` decimal(38, 2) NULL DEFAULT NULL,
  `order_status` enum('CREATED','PAID','PREPARING','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'CREATED' COMMENT '订单状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `store_id` int NULL DEFAULT NULL COMMENT '门店ID',
  `pickup_time` datetime NULL DEFAULT NULL COMMENT '预计取餐时间',
  `order_type` enum('QUICK','DELIVERY','STORE','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `order_number`(`order_number` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `order_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`User_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (1, 5, 'SB20250630001', 80.00, 'COMPLETED', '2025-07-02 16:57:02', '2025-06-30 10:30:45', 1, '2025-06-30 11:00:00', 'QUICK');
INSERT INTO `order` VALUES (2, 5, 'SB20250630002', 109.00, 'PREPARING', '2025-07-02 16:57:02', '2025-06-30 14:20:30', 2, '2025-06-30 15:00:00', 'DELIVERY');
INSERT INTO `order` VALUES (3, 2, 'SB20250630003', 226.50, 'PAID', '2025-07-02 16:57:02', '2025-06-30 16:45:12', 3, '2025-06-30 20:00:00', 'STORE');

-- ----------------------------
-- Table structure for order_detail
-- ----------------------------
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail`  (
  `detail_id` int NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` int NOT NULL COMMENT '订单ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（下单时快照）',
  `product_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片（下单时快照）',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '商品数量',
  `unit_price` decimal(38, 2) NULL DEFAULT NULL,
  `subtotal` decimal(38, 2) NULL DEFAULT NULL,
  `customization` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '定制要求（如温度、甜度等）',
  PRIMARY KEY (`detail_id`) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `order_detail_fk` FOREIGN KEY (`order_id`) REFERENCES `order` (`order_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `product_fk` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_detail
-- ----------------------------
INSERT INTO `order_detail` VALUES (1, 1, 1, '美式咖啡', 'http://localhost:8080/images/商品1.jpg', 2, 25.00, 50.00, '少冰');
INSERT INTO `order_detail` VALUES (2, 1, 2, '拿铁咖啡', 'http://localhost:8080/images/商品2.jpg', 1, 30.00, 30.00, NULL);
INSERT INTO `order_detail` VALUES (3, 2, 3, '抹茶拿铁', 'http://localhost:8080/images/商品3.jpg', 2, 35.50, 71.00, '少糖');
INSERT INTO `order_detail` VALUES (4, 2, 4, '星巴克蛋糕', 'http://localhost:8080/images/商品4.jpg', 1, 38.00, 38.00, NULL);
INSERT INTO `order_detail` VALUES (5, 3, 3, '抹茶拿铁', 'http://localhost:8080/images/商品5.jpg', 3, 35.50, 106.50, '正常糖');
INSERT INTO `order_detail` VALUES (6, 3, 5, '经典马克杯', 'http://localhost:8080/images/商品6.jpg', 1, 120.00, 120.00, NULL);

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `product_id` int NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `price` decimal(38, 2) NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '商品图片URL',
  `thumbnail_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '缩略图URL',
  `is_available` tinyint(1) NULL DEFAULT 1 COMMENT '是否上架（1-是，0-否）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (1, '美式咖啡', 25.00, '经典美式咖啡，浓郁香醇', 'COFFEE', 'http://localhost:8080/images/商品1.jpg', 'http://localhost:8080/images/商品1.jpg', 1, '2025-06-30 18:54:05', '2025-07-03 08:47:26');
INSERT INTO `product` VALUES (2, '拿铁咖啡', 30.00, '意式浓缩咖啡与蒸煮牛奶的完美结合', 'COFFEE', 'http://localhost:8080/images/商品2.jpg', 'http://localhost:8080/images/商品2.jpg', 1, '2025-06-30 18:54:05', '2025-07-02 18:39:24');
INSERT INTO `product` VALUES (3, '抹茶拿铁', 35.50, '日式抹茶与新鲜牛奶的融合', 'TEA', 'http://localhost:8080/images/商品3.jpg', 'http://localhost:8080/images/商品3.jpg', 1, '2025-06-30 18:54:05', '2025-07-02 18:39:29');
INSERT INTO `product` VALUES (4, '星巴克蛋糕', 38.00, '精选原料制作的精美蛋糕', 'DESSERT', 'http://localhost:8080/images/商品4.jpg', 'http://localhost:8080/images/商品4.jpg', 1, '2025-06-30 18:54:05', '2025-07-02 18:39:31');
INSERT INTO `product` VALUES (5, '经典马克杯', 120.00, '星巴克经典logo马克杯', 'MERCHANDISE', 'http://localhost:8080/images/商品5.jpg', 'http://localhost:8080/images/商品5.jpg', 1, '2025-06-30 18:54:05', '2025-07-02 18:39:34');

-- ----------------------------
-- Table structure for store
-- ----------------------------
DROP TABLE IF EXISTS `store`;
CREATE TABLE `store`  (
  `store_id` int NOT NULL AUTO_INCREMENT,
  `store_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`store_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of store
-- ----------------------------
INSERT INTO `store` VALUES (1, '人民公园店', '上海市黄浦区南京西路189号（近人民公园北门）');
INSERT INTO `store` VALUES (2, '南京西路店', '上海市静安区南京西路1600号');
INSERT INTO `store` VALUES (3, '陆家嘴店', '上海市浦东新区陆家嘴环路123号');

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `User_id` int NOT NULL AUTO_INCREMENT COMMENT '用户的编号，从1开始升序排序，每个用户都有唯一的ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户账号名，在登录系统和找回密码时需要',
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户账号唯一对应的登录密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户的网络昵称，可随时在个人中心修改',
  `money` int NULL DEFAULT NULL COMMENT '用户星礼卡总余额，可使用余额支付',
  `User_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户的联系电话，可用于活动报名联系以及领养宠物联系',
  `User_birth` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户生日',
  `wx_openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像URL',
  PRIMARY KEY (`User_id`) USING BTREE,
  UNIQUE INDEX `UK_9gb3fgmtg5icl9fut1yhsk8q5`(`wx_openid` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_user_phone`(`User_phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_info
-- ----------------------------
INSERT INTO `user_info` VALUES (1, 'coffee_lover', 'securepass123', '咖啡爱好者', 2500, '13800138000', '1990-05-15', NULL, NULL);
INSERT INTO `user_info` VALUES (2, 'java_dev', 'javap@ss', 'Java开发者', 1800, '13900139000', '1995-11-22', NULL, NULL);
INSERT INTO `user_info` VALUES (3, 'latte_artist', 'art!coffee', '拿铁艺术家', 3200, '13700137000', '1988-03-08', NULL, NULL);
INSERT INTO `user_info` VALUES (4, 'mocha_fan', 'm0ch@2023', '摩卡粉丝', 500, '13600136000', '2000-07-19', NULL, NULL);
INSERT INTO `user_info` VALUES (5, 'user', '123456', '帅铮', 200, '123456789', NULL, NULL, 'http://localhost:8080/avatars/1.png');

SET FOREIGN_KEY_CHECKS = 1;
