import { ElMessageBox } from 'element-plus'

/**
 * 删除确认，返回是否确认。取消时不抛错，直接返回 false。
 */
export async function confirmDelete(message = '确定删除这条记录吗？', title = '删除确认'): Promise<boolean> {
  try {
    await ElMessageBox.confirm(message, title, {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
    })
    return true
  } catch {
    return false
  }
}
