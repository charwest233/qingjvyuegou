<template>
  <div class="page-container py-4">
    <div class="flex items-center gap-3 mb-4">
      <button @click="$router.back()" class="p-2 -ml-2 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer">
        <ArrowLeft class="w-5 h-5 text-gray-600" />
      </button>
      <h1 class="section-title !mb-0">设置</h1>
    </div>

    <!-- 头像 -->
    <div class="bg-white rounded-2xl p-4 shadow-sm mb-3">
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-700">头像</span>
        <div class="flex items-center gap-3 cursor-pointer" @click="showAvatarEdit = true">
          <div class="w-12 h-12 rounded-full bg-gray-100 overflow-hidden">
            <img v-if="userInfo?.avatarUrl" :src="userInfo.avatarUrl" class="w-full h-full object-cover" />
            <User v-else class="w-6 h-6 mx-auto mt-3 text-gray-400" />
          </div>
          <ChevronRight class="w-4 h-4 text-gray-400" />
        </div>
      </div>
    </div>

    <!-- 昵称 -->
    <div class="bg-white rounded-2xl p-4 shadow-sm mb-3">
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-700">昵称</span>
        <div class="flex items-center gap-2 cursor-pointer" @click="showNicknameEdit = true">
          <span class="text-sm text-gray-900">{{ userInfo?.nickname || '未设置' }}</span>
          <ChevronRight class="w-4 h-4 text-gray-400" />
        </div>
      </div>
    </div>

    <!-- 手机号 -->
    <div class="bg-white rounded-2xl p-4 shadow-sm mb-3">
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-700">手机号</span>
        <div class="flex items-center gap-2 cursor-pointer" @click="showPhoneEdit = true">
          <span class="text-sm text-gray-900">{{ userInfo?.phone || '未绑定' }}</span>
          <ChevronRight class="w-4 h-4 text-gray-400" />
        </div>
      </div>
    </div>

    <!-- 邮箱 -->
    <div class="bg-white rounded-2xl p-4 shadow-sm mb-3">
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-700">邮箱</span>
        <div class="flex items-center gap-2 cursor-pointer" @click="showEmailEdit = true">
          <span class="text-sm text-gray-900">{{ userInfo?.email || '未绑定' }}</span>
          <ChevronRight class="w-4 h-4 text-gray-400" />
        </div>
      </div>
    </div>

    <!-- 注册时间 -->
    <div class="bg-white rounded-2xl p-4 shadow-sm">
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-700">注册时间</span>
        <span class="text-sm text-gray-400">{{ userInfo?.createTime || '-' }}</span>
      </div>
    </div>

    <!-- 管理入口（仅管理员可见） -->
    <div v-if="isAdmin" class="mt-6">
      <h3 class="text-xs text-gray-400 mb-2 px-1">管理功能</h3>
      <router-link to="/admin/reviews" class="bg-white rounded-2xl p-4 shadow-sm flex items-center justify-between hover:bg-gray-50 transition-colors">
        <span class="text-sm text-gray-700 flex items-center gap-2">
          <MessageSquare class="w-4 h-4 text-primary" />
          评价管理
        </span>
        <ChevronRight class="w-4 h-4 text-gray-400" />
      </router-link>
    </div>

    <!-- 头像编辑对话框 -->
    <el-dialog v-model="showAvatarEdit" title="修改头像" width="92%" top="5vh" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="头像URL">
          <el-input v-model="avatarUrl" placeholder="请输入头像图片URL" />
        </el-form-item>
        <div v-if="avatarUrl" class="flex justify-center mb-4">
          <div class="w-24 h-24 rounded-full overflow-hidden border-2 border-gray-200">
            <img :src="avatarUrl" class="w-full h-full object-cover" @error="avatarError = true" />
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showAvatarEdit = false">取消</el-button>
        <el-button type="primary" :loading="savingAvatar" @click="handleSaveAvatar">保存</el-button>
      </template>
    </el-dialog>

    <!-- 昵称编辑对话框 -->
    <el-dialog v-model="showNicknameEdit" title="修改昵称" width="92%" top="5vh" destroy-on-close>
      <el-input v-model="nickname" placeholder="请输入昵称" maxlength="20" />
      <template #footer>
        <el-button @click="showNicknameEdit = false">取消</el-button>
        <el-button type="primary" :loading="savingNickname" @click="handleSaveNickname">保存</el-button>
      </template>
    </el-dialog>

    <!-- 手机号编辑对话框 -->
    <el-dialog v-model="showPhoneEdit" title="修改手机号" width="92%" top="5vh" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="新手机号">
          <el-input v-model="newPhone" placeholder="请输入新手机号" maxlength="11" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPhoneEdit = false">取消</el-button>
        <el-button type="primary" :loading="savingPhone" @click="handleSavePhone">保存</el-button>
      </template>
    </el-dialog>

    <!-- 邮箱编辑对话框 -->
    <el-dialog v-model="showEmailEdit" title="修改邮箱" width="92%" top="5vh" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="新邮箱">
          <el-input v-model="newEmail" placeholder="请输入新邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEmailEdit = false">取消</el-button>
        <el-button type="primary" :loading="savingEmail" @click="handleSaveEmail">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ArrowLeft, User, ChevronRight, MessageSquare } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { getUser } from '@/utils/auth'
import { updateUserProfile, updateUserPhone, updateUserEmail } from '@/api/user'

const userInfo = computed(() => getUser())
const isAdmin = computed(() => {
  const user = getUser()
  return user && Number(user.type) === 1
})

// 头像
const showAvatarEdit = ref(false)
const avatarUrl = ref('')
const savingAvatar = ref(false)
async function handleSaveAvatar() {
  if (!avatarUrl.value) {
    ElMessage.warning('请输入头像URL')
    return
  }
  savingAvatar.value = true
  try {
    const res = await updateUserProfile({ avatarUrl: avatarUrl.value })
    if (res.code === 200) {
      const info = getUser()
      info.avatarUrl = avatarUrl.value
      localStorage.setItem('user_info', JSON.stringify(info))
      ElMessage.success('头像已更新')
      showAvatarEdit.value = false
    }
  } catch (err) {
    console.error(err)
  } finally {
    savingAvatar.value = false
  }
}

// 昵称
const showNicknameEdit = ref(false)
const nickname = ref('')
const savingNickname = ref(false)
async function handleSaveNickname() {
  if (!nickname.value) {
    ElMessage.warning('请输入昵称')
    return
  }
  savingNickname.value = true
  try {
    const res = await updateUserProfile({ nickname: nickname.value })
    if (res.code === 200) {
      const info = getUser()
      info.nickname = nickname.value
      localStorage.setItem('user_info', JSON.stringify(info))
      ElMessage.success('昵称已更新')
      showNicknameEdit.value = false
    }
  } catch (err) {
    console.error(err)
  } finally {
    savingNickname.value = false
  }
}

// 手机号
const showPhoneEdit = ref(false)
const newPhone = ref('')
const savingPhone = ref(false)
async function handleSavePhone() {
  if (!newPhone.value || newPhone.value.length < 11) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  savingPhone.value = true
  try {
    const res = await updateUserPhone(newPhone.value)
    if (res.code === 200) {
      const info = getUser()
      info.phone = newPhone.value
      localStorage.setItem('user_info', JSON.stringify(info))
      ElMessage.success('手机号已更新')
      showPhoneEdit.value = false
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '修改失败')
  } finally {
    savingPhone.value = false
  }
}

// 邮箱
const showEmailEdit = ref(false)
const newEmail = ref('')
const savingEmail = ref(false)
async function handleSaveEmail() {
  if (!newEmail.value) {
    ElMessage.warning('请输入邮箱')
    return
  }
  savingEmail.value = true
  try {
    const res = await updateUserEmail(newEmail.value)
    if (res.code === 200) {
      const info = getUser()
      info.email = newEmail.value
      localStorage.setItem('user_info', JSON.stringify(info))
      ElMessage.success('邮箱已更新')
      showEmailEdit.value = false
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '修改失败')
  } finally {
    savingEmail.value = false
  }
}
</script>
