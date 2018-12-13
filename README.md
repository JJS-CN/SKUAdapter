# SKUAdapter
sku商品属性多选情况，封装成adapter；供recyclerview使用，基于baseRecyclerViewHodler开源库

[![](https://www.jitpack.io/v/JJS-CN/SKUAdapter.svg)](https://www.jitpack.io/#JJS-CN/SKUAdapter)

### 在项目目录下gradle添加仓库地址
    allprojects {
		repositories {

			maven { url 'https://www.jitpack.io' }
		}
	}

### 在module目录下gradle添加项目地址
    或者将latest.integration 改为对应版本号
	dependencies {
	            implementation 'com.github.JJS-CN:SKUAdapter:latest.integration'
    	}
##### v1.2.1
      修复只有一个属性，多个选项但只出现一个的问题；由于逻辑处理时取值错误
