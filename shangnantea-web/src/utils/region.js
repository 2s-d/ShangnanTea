// 从官方库导入中国省市区数据
import { provinceAndCityData, regionData, codeToText as CodeToText } from 'element-china-area-data'

// 导出所有可能用到的数据
export {
  // 省市二级联动数据
  provinceAndCityData,
  // 省市区三级联动数据
  regionData,
  // 编码转文本对象
  CodeToText
}

// 添加获取区域数据的方法
export function getStaticRegionData() {
  return regionData
}

// 默认导出省市区三级联动数据
export default regionData 