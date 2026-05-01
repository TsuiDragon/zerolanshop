<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  BarChart3,
  Banknote,
  Bell,
  Box,
  ChartNoAxesCombined,
  ChevronDown,
  ChevronRight,
  CreditCard,
  DollarSign,
  Edit3,
  FileText,
  HandCoins,
  Home,
  KeyRound,
  Megaphone,
  Package,
  Plus,
  ReceiptText,
  Settings,
  ShoppingCart,
  Trash2,
  TrendingUp,
  UserPlus,
  UserRound,
  Users,
  WalletCards,
  X,
} from 'lucide-vue-next'

type Page = 'admin' | 'adminAuth' | 'home' | 'userAuth'
type AdminView = 'home' | 'category' | 'pricingTemplate' | 'mediaAsset'
type AuthMode = 'login' | 'register'
type PricingType = 'PERCENTAGE' | 'FIXED_AMOUNT'

type ApiResult<T> = {
  code: number
  message: string
  data: T
}

type AdminLoginResponse = {
  token: string
  adminId: number
  username: string
  nickname: string
  role: string
}

type LoginResponse = {
  token: string
  userId: number
  username: string
}

type ImageUploadResponse = {
  id: number
  url: string
  filename: string
  size: number
  contentType: string
}

type MediaAssetResponse = {
  id: number
  scene: 'category' | 'product'
  url: string
  filename: string
  originalName?: string | null
  contentType: string
  extension: string
  size: number
  width?: number | null
  height?: number | null
  status: number
  used: boolean
  createTime?: string
  updateTime?: string
}

type MediaAssetFilters = {
  scene: '' | 'category' | 'product'
  filename: string
  used: '' | 'true' | 'false'
}

type CategoryResponse = {
  id: number
  parentId: number
  name: string
  icon?: string | null
  sort: number
  status: number
  createTime?: string
  updateTime?: string
}

type CategoryTreeResponse = CategoryResponse & {
  children: CategoryResponse[]
}

type CategoryRow = CategoryResponse & {
  level: 1 | 2
  children?: CategoryResponse[]
}

type CategoryForm = {
  id?: number
  parentId: number
  name: string
  icon: string
  sort?: number
  status: number
}

type PricingTemplateResponse = {
  id: number
  name: string
  pricingType: PricingType
  pricingValue: number
  description?: string | null
  sort: number
  status: number
  createTime?: string
  updateTime?: string
}

type PricingTemplateForm = {
  id?: number
  name: string
  pricingType: PricingType
  pricingValue: number
  description: string
  sort: number
  status: number
}

type PricingTemplateFilters = {
  name: string
  pricingType: '' | PricingType
  status: '' | 0 | 1
}

const currentPath = ref(window.location.pathname)
const adminView = ref<AdminView>(getAdminViewFromPath(currentPath.value))

const page = computed<Page>(() => {
  if (currentPath.value === '/login') return 'userAuth'
  if (currentPath.value === '/admin/login') return 'adminAuth'
  if (
    currentPath.value === '/admin' ||
    currentPath.value === '/admin/index' ||
    currentPath.value === '/admin/goods/category' ||
    currentPath.value === '/admin/goods/pricing-template' ||
    currentPath.value === '/admin/goods/media-assets'
  ) return 'admin'
  if (currentPath.value === '/index') return 'home'
  return 'userAuth'
})

const mode = ref<AuthMode>('login')
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const email = ref('')
const phone = ref('')
const remember = ref(localStorage.getItem('zerolanshop_remember') === 'true')
const loading = ref(false)
const message = ref('')
const currentUsername = ref(readStoredUsername())

const adminUsername = ref('')
const adminPassword = ref('')
const adminLoading = ref(false)
const adminMessage = ref('')
const currentAdminName = ref(readStoredAdminName())

const categoryTree = ref<CategoryTreeResponse[]>([])
const expandedIds = ref<number[]>([])
const selectedIds = ref<number[]>([])
const categoryLoading = ref(false)
const categoryMessage = ref('')
const savingCategory = ref(false)
const categoryIconUploading = ref(false)
const modalOpen = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const categoryForm = reactive<CategoryForm>({
  parentId: 0,
  name: '',
  icon: '',
  status: 1,
})

const pricingTemplates = ref<PricingTemplateResponse[]>([])
const pricingTemplateLoading = ref(false)
const pricingTemplateMessage = ref('')
const savingPricingTemplate = ref(false)
const pricingTemplateModalOpen = ref(false)
const pricingTemplateModalMode = ref<'create' | 'edit'>('create')
const pricingTemplateFilters = reactive<PricingTemplateFilters>({
  name: '',
  pricingType: '',
  status: '',
})
const pricingTemplateForm = reactive<PricingTemplateForm>({
  name: '',
  pricingType: 'PERCENTAGE',
  pricingValue: 0,
  description: '',
  sort: 0,
  status: 1,
})
const mediaAssets = ref<MediaAssetResponse[]>([])
const mediaAssetLoading = ref(false)
const mediaAssetMessage = ref('')
const mediaAssetUploading = ref(false)
const mediaAssetPickerOpen = ref(false)
const mediaAssetFilters = reactive<MediaAssetFilters>({
  scene: '',
  filename: '',
  used: '',
})

const adminMenus = [
  { name: '首页', icon: Home, view: 'home' as AdminView },
  {
    name: '商品',
    icon: Package,
    expanded: true,
    children: [
      { name: '商品管理' },
      { name: '商品分类', view: 'category' as AdminView },
      { name: '定价模板', view: 'pricingTemplate' as AdminView },
      { name: '素材管理', view: 'mediaAsset' as AdminView },
      { name: '货源渠道' },
    ],
  },
  { name: '用户', icon: Users },
  { name: '订单', icon: ShoppingCart },
  { name: '数据', icon: BarChart3 },
  { name: '运营', icon: Megaphone },
  { name: '设置', icon: Settings },
]

const todayStats = [
  { label: '今日销售额', value: '￥58,790', icon: DollarSign, tone: 'blue' },
  { label: '今日订单数量', value: '165', icon: ShoppingCart, tone: 'green' },
  { label: '今日利润', value: '￥12,450', icon: WalletCards, tone: 'purple' },
  { label: '今日新增用户', value: '23', icon: UserPlus, tone: 'orange' },
]

const monthStats = [
  { label: '本月销售额', value: '￥1,458,790', icon: TrendingUp, tone: 'pink' },
  { label: '本月订单数量', value: '4,127', icon: Box, tone: 'indigo' },
  { label: '本月利润', value: '￥325,680', icon: CreditCard, tone: 'teal' },
  { label: '本月新增用户', value: '589', icon: Users, tone: 'amber' },
]

const fallbackCategories: CategoryTreeResponse[] = [
  {
    id: 5,
    parentId: 0,
    name: '视频会员',
    icon: 'TV',
    sort: 6,
    status: 1,
    children: [
      { id: 6, parentId: 5, name: '爱奇艺', icon: 'IQ', sort: 1, status: 1 },
      { id: 16, parentId: 5, name: '腾讯视频', icon: 'TX', sort: 2, status: 1 },
      { id: 17, parentId: 5, name: '优酷视频', icon: 'YK', sort: 3, status: 0 },
    ],
  },
  { id: 7, parentId: 0, name: '音乐会员', icon: 'MU', sort: 10, status: 1, children: [] },
  { id: 157, parentId: 0, name: '游戏充值', icon: 'GM', sort: 15, status: 1, children: [] },
  { id: 200, parentId: 0, name: '生活缴费', icon: 'LF', sort: 20, status: 0, children: [] },
]

const visibleRows = computed<CategoryRow[]>(() => {
  const rows: CategoryRow[] = []
  for (const category of categoryTree.value) {
    rows.push({ ...category, level: 1 })
    if (expandedIds.value.includes(category.id)) {
      for (const child of category.children || []) {
        rows.push({ ...child, level: 2 })
      }
    }
  }
  return rows
})

const totalCategoryCount = computed(() =>
  categoryTree.value.reduce((total, category) => total + 1 + (category.children?.length || 0), 0),
)

const allVisibleSelected = computed(
  () => visibleRows.value.length > 0 && visibleRows.value.every((row) => selectedIds.value.includes(row.id)),
)

const parentOptions = computed(() => categoryTree.value.map(({ id, name }) => ({ id, name })))

onMounted(() => {
  normalizeRoute()
  syncAdminViewFromPath()
})

function normalizeRoute() {
  if (currentPath.value === '/') {
    window.history.replaceState({}, '', '/login')
    currentPath.value = '/login'
  }
  if (currentPath.value === '/home') {
    window.history.replaceState({}, '', '/index')
    currentPath.value = '/index'
  }
  if (currentPath.value === '/admin') {
    window.history.replaceState({}, '', '/admin/login')
    currentPath.value = '/admin/login'
  }
}

window.addEventListener('popstate', () => {
  currentPath.value = window.location.pathname
  normalizeRoute()
  syncAdminViewFromPath()
})

function navigate(path: string) {
  window.history.pushState({}, '', path)
  currentPath.value = path
  normalizeRoute()
  syncAdminViewFromPath()
}

function switchMode(nextMode: AuthMode) {
  mode.value = nextMode
  message.value = ''
  password.value = ''
  confirmPassword.value = ''
}

async function handleUserSubmit() {
  if (!username.value.trim() || !password.value || loading.value) return

  loading.value = true
  message.value = ''

  try {
    if (mode.value === 'register') validateRegisterForm()

    const result = await requestJson<LoginResponse>(mode.value === 'register' ? '/api/auth/register' : '/api/auth/sessions', {
      method: 'POST',
      auth: false,
      body:
        mode.value === 'register'
          ? {
              username: username.value.trim(),
              password: password.value,
              confirmPassword: confirmPassword.value,
              email: email.value.trim() || null,
              phone: phone.value.trim() || null,
            }
          : {
              username: username.value.trim(),
              password: password.value,
            },
    })

    const storage = remember.value ? localStorage : sessionStorage
    storage.setItem('zerolanshop_token', result.data.token)
    storage.setItem('zerolanshop_user', JSON.stringify({ userId: result.data.userId, username: result.data.username }))
    localStorage.setItem('zerolanshop_remember', String(remember.value))
    currentUsername.value = result.data.username
    navigate('/index')
  } catch (error) {
    message.value = error instanceof Error ? error.message : '登录失败，请稍后再试'
  } finally {
    loading.value = false
  }
}

function validateRegisterForm() {
  if (username.value.trim().length < 3) throw new Error('用户名至少 3 个字符')
  if (password.value.length < 6) throw new Error('密码至少 6 个字符')
  if (password.value !== confirmPassword.value) throw new Error('两次输入的密码不一致')
  if (!email.value.trim() && !phone.value.trim()) throw new Error('请填写手机号或邮箱')
}

function switchAdminView(view: AdminView) {
  navigate(getAdminPath(view))
}

function getAdminPath(view: AdminView) {
  if (view === 'category') return '/admin/goods/category'
  if (view === 'pricingTemplate') return '/admin/goods/pricing-template'
  if (view === 'mediaAsset') return '/admin/goods/media-assets'
  return '/admin/index'
}

function getAdminViewFromPath(path: string): AdminView {
  if (path === '/admin/goods/category') return 'category'
  if (path === '/admin/goods/pricing-template') return 'pricingTemplate'
  if (path === '/admin/goods/media-assets') return 'mediaAsset'
  return 'home'
}

function syncAdminViewFromPath() {
  if (page.value !== 'admin') return
  adminView.value = getAdminViewFromPath(currentPath.value)
  if (adminView.value === 'category' && categoryTree.value.length === 0) {
    loadCategories()
  }
  if (adminView.value === 'pricingTemplate' && pricingTemplates.value.length === 0) {
    loadPricingTemplates()
  }
  if (adminView.value === 'mediaAsset' && mediaAssets.value.length === 0) {
    loadMediaAssets()
  }
}

function isGoodsAdminView() {
  return ['category', 'pricingTemplate', 'mediaAsset'].includes(adminView.value)
}

async function handleAdminSubmit() {
  if (!adminUsername.value.trim() || !adminPassword.value || adminLoading.value) return

  adminLoading.value = true
  adminMessage.value = ''

  try {
    const result = await requestJson<AdminLoginResponse>('/api/admin/sessions', {
      method: 'POST',
      body: {
        username: adminUsername.value.trim(),
        password: adminPassword.value,
      },
      auth: false,
    })
    localStorage.setItem('zerolanshop_admin_token', result.data.token)
    localStorage.setItem('zerolanshop_admin', JSON.stringify(result.data))
    currentAdminName.value = result.data.nickname || result.data.username
    adminView.value = 'home'
    navigate('/admin/index')
  } catch (error) {
    adminMessage.value = error instanceof Error ? error.message : '管理员登录失败，请稍后再试'
  } finally {
    adminLoading.value = false
  }
}

async function loadCategories() {
  categoryLoading.value = true
  categoryMessage.value = ''

  try {
    const result = await requestJson<CategoryTreeResponse[]>('/api/admin/categories/tree')
    categoryTree.value = normalizeTree(result.data)
    expandedIds.value = categoryTree.value.filter((category) => category.children?.length).map((category) => category.id)
  } catch (error) {
    categoryTree.value = fallbackCategories
    expandedIds.value = [5]
    categoryMessage.value = error instanceof Error ? `接口暂不可用，当前展示示例数据：${error.message}` : '接口暂不可用，当前展示示例数据'
  } finally {
    categoryLoading.value = false
  }
}

function normalizeTree(tree: CategoryTreeResponse[]) {
  return [...tree]
    .sort(sortCategory)
    .map((category) => ({
      ...category,
      children: [...(category.children || [])].sort(sortCategory),
    }))
}

function sortCategory(a: CategoryResponse, b: CategoryResponse) {
  return (a.sort || 0) - (b.sort || 0) || a.id - b.id
}

function toggleExpand(id: number) {
  expandedIds.value = expandedIds.value.includes(id)
    ? expandedIds.value.filter((item) => item !== id)
    : [...expandedIds.value, id]
}

function toggleSelect(id: number) {
  selectedIds.value = selectedIds.value.includes(id)
    ? selectedIds.value.filter((item) => item !== id)
    : [...selectedIds.value, id]
}

function toggleSelectAll() {
  selectedIds.value = allVisibleSelected.value ? [] : visibleRows.value.map((row) => row.id)
}

function openCreateModal(parentId = 0) {
  modalMode.value = 'create'
  Object.assign(categoryForm, {
    id: undefined,
    parentId,
    name: '',
    icon: '',
    sort: undefined,
    status: 1,
  })
  modalOpen.value = true
}

function openEditModal(category: CategoryResponse) {
  modalMode.value = 'edit'
  Object.assign(categoryForm, {
    id: category.id,
    parentId: category.parentId,
    name: category.name,
    icon: category.icon || '',
    sort: category.sort,
    status: category.status,
  })
  modalOpen.value = true
}

async function saveCategory() {
  if (!categoryForm.name.trim() || savingCategory.value) return

  savingCategory.value = true
  categoryMessage.value = ''

  try {
    if (modalMode.value === 'create') {
      await requestJson<CategoryResponse>('/api/admin/categories', {
        method: 'POST',
        body: {
          parentId: categoryForm.parentId,
          name: categoryForm.name.trim(),
          icon: categoryForm.icon.trim() || null,
          status: categoryForm.status,
        },
      })
    } else {
      await requestJson<CategoryResponse>(`/api/admin/categories/${categoryForm.id}`, {
        method: 'PUT',
        body: {
          parentId: categoryForm.parentId,
          name: categoryForm.name.trim(),
          icon: categoryForm.icon.trim() || null,
          sort: categoryForm.sort,
          status: categoryForm.status,
        },
      })
    }
    modalOpen.value = false
    await loadCategories()
  } catch (error) {
    categoryMessage.value = error instanceof Error ? error.message : '保存分类失败'
  } finally {
    savingCategory.value = false
  }
}

async function changeStatus(category: CategoryResponse) {
  const nextStatus = category.status === 1 ? 0 : 1
  category.status = nextStatus
  try {
    await requestJson<CategoryResponse>(`/api/admin/categories/${category.id}/status`, {
      method: 'PATCH',
      body: { status: nextStatus },
    })
  } catch (error) {
    category.status = nextStatus === 1 ? 0 : 1
    categoryMessage.value = error instanceof Error ? error.message : '状态更新失败'
  }
}

async function updateSort(category: CategoryResponse, event: Event) {
  const input = event.target as HTMLInputElement
  const nextSort = Number(input.value)
  if (Number.isNaN(nextSort) || nextSort < 0 || nextSort === category.sort) return

  const previousSort = category.sort
  category.sort = nextSort
  try {
    await requestJson<CategoryResponse>(`/api/admin/categories/${category.id}`, {
      method: 'PUT',
      body: {
        parentId: category.parentId,
        name: category.name,
        icon: category.icon || null,
        sort: nextSort,
        status: category.status,
      },
    })
    await loadCategories()
  } catch (error) {
    category.sort = previousSort
    categoryMessage.value = error instanceof Error ? error.message : '排序更新失败'
  }
}

async function deleteCategory(category: CategoryResponse) {
  if (!window.confirm(`确定删除分类“${category.name}”吗？`)) return
  categoryMessage.value = ''
  try {
    await requestJson<void>(`/api/admin/categories/${category.id}`, { method: 'DELETE' })
    selectedIds.value = selectedIds.value.filter((id) => id !== category.id)
    await loadCategories()
  } catch (error) {
    categoryMessage.value = error instanceof Error ? error.message : '删除分类失败'
  }
}

async function batchDelete() {
  if (!selectedIds.value.length) return
  if (!window.confirm(`确定删除选中的 ${selectedIds.value.length} 个分类吗？`)) return

  categoryMessage.value = ''
  try {
    for (const id of selectedIds.value) {
      await requestJson<void>(`/api/admin/categories/${id}`, { method: 'DELETE' })
    }
    selectedIds.value = []
    await loadCategories()
  } catch (error) {
    categoryMessage.value = error instanceof Error ? error.message : '批量删除失败'
  }
}

async function uploadCategoryIcon(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || categoryIconUploading.value) return

  categoryIconUploading.value = true
  categoryMessage.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('scene', 'category')
    const result = await requestForm<ImageUploadResponse>('/api/admin/files/images', formData)
    categoryForm.icon = result.data.url
    await loadMediaAssets()
  } catch (error) {
    categoryMessage.value = error instanceof Error ? error.message : '分类图标上传失败'
  } finally {
    categoryIconUploading.value = false
    input.value = ''
  }
}

async function uploadMediaAsset(event: Event, scene: 'category' | 'product' = 'category') {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || mediaAssetUploading.value) return

  mediaAssetUploading.value = true
  mediaAssetMessage.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('scene', scene)
    await requestForm<ImageUploadResponse>('/api/admin/files/images', formData)
    await loadMediaAssets()
  } catch (error) {
    mediaAssetMessage.value = error instanceof Error ? error.message : '素材上传失败'
  } finally {
    mediaAssetUploading.value = false
    input.value = ''
  }
}

async function loadMediaAssets() {
  mediaAssetLoading.value = true
  mediaAssetMessage.value = ''

  try {
    const params = new URLSearchParams()
    if (mediaAssetFilters.scene) params.set('scene', mediaAssetFilters.scene)
    if (mediaAssetFilters.filename.trim()) params.set('filename', mediaAssetFilters.filename.trim())
    if (mediaAssetFilters.used) params.set('used', mediaAssetFilters.used)
    const query = params.toString()
    const result = await requestJson<MediaAssetResponse[]>(`/api/admin/media-assets${query ? `?${query}` : ''}`)
    mediaAssets.value = result.data
  } catch (error) {
    mediaAssetMessage.value = error instanceof Error ? error.message : '素材加载失败'
  } finally {
    mediaAssetLoading.value = false
  }
}

function resetMediaAssetFilters() {
  Object.assign(mediaAssetFilters, {
    scene: '',
    filename: '',
    used: '',
  })
  loadMediaAssets()
}

function openMediaAssetPicker() {
  mediaAssetPickerOpen.value = true
  mediaAssetFilters.scene = 'category'
  loadMediaAssets()
}

function selectMediaAsset(asset: MediaAssetResponse) {
  categoryForm.icon = asset.url
  mediaAssetPickerOpen.value = false
}

async function deleteMediaAsset(asset: MediaAssetResponse) {
  if (!window.confirm(`确定删除素材“${asset.originalName || asset.filename}”吗？未使用素材会同时删除磁盘文件。`)) return

  mediaAssetMessage.value = ''
  try {
    await requestJson<void>(`/api/admin/media-assets/${asset.id}`, { method: 'DELETE' })
    await loadMediaAssets()
  } catch (error) {
    mediaAssetMessage.value = error instanceof Error ? error.message : '素材删除失败'
  }
}

async function copyMediaAssetUrl(asset: MediaAssetResponse) {
  try {
    await navigator.clipboard.writeText(asset.url)
    mediaAssetMessage.value = '素材地址已复制'
  } catch {
    mediaAssetMessage.value = asset.url
  }
}

function sceneLabel(scene: string) {
  return scene === 'category' ? '分类图标' : '商品图片'
}

function formatFileSize(size: number) {
  if (size >= 1024 * 1024) return `${(size / 1024 / 1024).toFixed(2)} MB`
  return `${(size / 1024).toFixed(1)} KB`
}

async function loadPricingTemplates() {
  pricingTemplateLoading.value = true
  pricingTemplateMessage.value = ''

  try {
    const params = new URLSearchParams()
    if (pricingTemplateFilters.name.trim()) params.set('name', pricingTemplateFilters.name.trim())
    if (pricingTemplateFilters.pricingType) params.set('pricingType', pricingTemplateFilters.pricingType)
    if (pricingTemplateFilters.status !== '') params.set('status', String(pricingTemplateFilters.status))
    const query = params.toString()
    const result = await requestJson<PricingTemplateResponse[]>(
      `/api/admin/pricing-templates${query ? `?${query}` : ''}`,
    )
    pricingTemplates.value = [...result.data].sort(sortPricingTemplate)
  } catch (error) {
    pricingTemplateMessage.value = error instanceof Error ? error.message : '定价模板加载失败'
  } finally {
    pricingTemplateLoading.value = false
  }
}

function sortPricingTemplate(a: PricingTemplateResponse, b: PricingTemplateResponse) {
  return (a.sort || 0) - (b.sort || 0) || a.id - b.id
}

function resetPricingTemplateFilters() {
  Object.assign(pricingTemplateFilters, {
    name: '',
    pricingType: '',
    status: '',
  })
  loadPricingTemplates()
}

function openCreatePricingTemplateModal() {
  pricingTemplateModalMode.value = 'create'
  Object.assign(pricingTemplateForm, {
    id: undefined,
    name: '',
    pricingType: 'PERCENTAGE',
    pricingValue: 0,
    description: '',
    sort: 0,
    status: 1,
  })
  pricingTemplateModalOpen.value = true
}

function openEditPricingTemplateModal(template: PricingTemplateResponse) {
  pricingTemplateModalMode.value = 'edit'
  Object.assign(pricingTemplateForm, {
    id: template.id,
    name: template.name,
    pricingType: template.pricingType,
    pricingValue: Number(template.pricingValue),
    description: template.description || '',
    sort: template.sort,
    status: template.status,
  })
  pricingTemplateModalOpen.value = true
}

async function savePricingTemplate() {
  if (!pricingTemplateForm.name.trim() || savingPricingTemplate.value) return

  savingPricingTemplate.value = true
  pricingTemplateMessage.value = ''

  const body = {
    name: pricingTemplateForm.name.trim(),
    pricingType: pricingTemplateForm.pricingType,
    pricingValue: Number(pricingTemplateForm.pricingValue),
    description: pricingTemplateForm.description.trim() || null,
    sort: Number(pricingTemplateForm.sort) || 0,
    status: pricingTemplateForm.status,
  }

  try {
    if (pricingTemplateModalMode.value === 'create') {
      await requestJson<PricingTemplateResponse>('/api/admin/pricing-templates', {
        method: 'POST',
        body,
      })
    } else {
      await requestJson<PricingTemplateResponse>(`/api/admin/pricing-templates/${pricingTemplateForm.id}`, {
        method: 'PUT',
        body,
      })
    }
    pricingTemplateModalOpen.value = false
    await loadPricingTemplates()
  } catch (error) {
    pricingTemplateMessage.value = error instanceof Error ? error.message : '定价模板保存失败'
  } finally {
    savingPricingTemplate.value = false
  }
}

async function changePricingTemplateStatus(template: PricingTemplateResponse) {
  const nextStatus = template.status === 1 ? 0 : 1
  template.status = nextStatus
  try {
    await requestJson<PricingTemplateResponse>(`/api/admin/pricing-templates/${template.id}/status`, {
      method: 'PATCH',
      body: { status: nextStatus },
    })
  } catch (error) {
    template.status = nextStatus === 1 ? 0 : 1
    pricingTemplateMessage.value = error instanceof Error ? error.message : '定价模板状态更新失败'
  }
}

async function deletePricingTemplate(template: PricingTemplateResponse) {
  if (!window.confirm(`确定删除定价模板“${template.name}”吗？`)) return

  pricingTemplateMessage.value = ''
  try {
    await requestJson<void>(`/api/admin/pricing-templates/${template.id}`, { method: 'DELETE' })
    await loadPricingTemplates()
  } catch (error) {
    pricingTemplateMessage.value = error instanceof Error ? error.message : '定价模板删除失败'
  }
}

function pricingTypeLabel(pricingType: PricingType) {
  return pricingType === 'PERCENTAGE' ? '百分比加价' : '固定金额加价'
}

function formatPricingValue(template: PricingTemplateResponse) {
  const value = Number(template.pricingValue)
  if (template.pricingType === 'PERCENTAGE') return `${formatPlainNumber(value)}%`
  return `+¥${value.toFixed(2)}`
}

function formatPlainNumber(value: number) {
  return Number.isInteger(value) ? String(value) : value.toFixed(4).replace(/0+$/, '').replace(/\.$/, '')
}

function formatDateTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

function isImageIcon(icon?: string | null) {
  return Boolean(icon && (/^https?:\/\//.test(icon) || icon.startsWith('/uploads/images/')))
}

function readStoredAdminName() {
  const stored = localStorage.getItem('zerolanshop_admin') || sessionStorage.getItem('zerolanshop_admin')
  if (!stored) return '山西零蓝车辆销售公司'
  try {
    const admin = JSON.parse(stored)
    return admin.nickname || admin.username || '山西零蓝车辆销售公司'
  } catch {
    return '山西零蓝车辆销售公司'
  }
}

function readStoredUsername() {
  const stored = localStorage.getItem('zerolanshop_user') || sessionStorage.getItem('zerolanshop_user')
  if (!stored) return 'ceshi'
  try {
    return JSON.parse(stored).username || 'ceshi'
  } catch {
    return 'ceshi'
  }
}

function readAdminToken() {
  return localStorage.getItem('zerolanshop_admin_token') || sessionStorage.getItem('zerolanshop_admin_token')
}

async function requestJson<T>(
  endpoint: string,
  options: { method?: string; body?: unknown; auth?: boolean } = {},
) {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  if (options.auth !== false) {
    const token = readAdminToken()
    if (token) headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(endpoint, {
    method: options.method || 'GET',
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })

  if (!response.ok) throw new Error(`请求失败：${response.status}`)
  const result = (await response.json()) as ApiResult<T>
  if (result.code !== 200) throw new Error(result.message || '请求失败')
  return result
}

async function requestForm<T>(
  endpoint: string,
  body: FormData,
  options: { method?: string; auth?: boolean } = {},
) {
  const headers: Record<string, string> = {}
  if (options.auth !== false) {
    const token = readAdminToken()
    if (token) headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(endpoint, {
    method: options.method || 'POST',
    headers,
    body,
  })

  if (!response.ok) throw new Error(`请求失败：${response.status}`)
  const result = (await response.json()) as ApiResult<T>
  if (result.code !== 200) throw new Error(result.message || '请求失败')
  return result
}
</script>

<template>
  <main v-if="page === 'userAuth'" class="login-page">
    <section class="login-panel buyer-login">
      <div class="buyer-mark">
        <ShoppingCart :size="32" />
      </div>
      <h1>零蓝权益采购平台</h1>
      <p>欢迎登录</p>

      <div class="auth-tabs">
        <button type="button" :class="{ active: mode === 'login' }" @click="switchMode('login')">登录</button>
        <button type="button" :class="{ active: mode === 'register' }" @click="switchMode('register')">注册</button>
      </div>

      <form class="login-form" @submit.prevent="handleUserSubmit">
        <label>
          <span>登录账号</span>
          <input v-model="username" autocomplete="username" placeholder="请输入账号" type="text" />
        </label>

        <label v-if="mode === 'register'">
          <span>邮箱</span>
          <input v-model="email" autocomplete="email" placeholder="请输入邮箱" type="email" />
        </label>

        <label v-if="mode === 'register'">
          <span>手机号</span>
          <input v-model="phone" autocomplete="tel" placeholder="请输入手机号" type="tel" />
        </label>

        <label>
          <span>登录密码</span>
          <input v-model="password" autocomplete="current-password" placeholder="请输入密码" type="password" />
        </label>

        <label v-if="mode === 'register'">
          <span>确认密码</span>
          <input v-model="confirmPassword" autocomplete="new-password" placeholder="请再次输入密码" type="password" />
        </label>

        <div class="login-options">
          <label>
            <input v-model="remember" type="checkbox" />
            <span>记住登录状态</span>
          </label>
          <a href="#" @click.prevent>忘记密码?</a>
        </div>

        <p v-if="message" class="form-message">{{ message }}</p>
        <button type="submit" :disabled="loading || !username.trim() || !password">
          {{ loading ? '处理中...' : mode === 'register' ? '注册并进入系统' : '登录采购系统' }}
        </button>
      </form>

      <p class="login-switch">
        <template v-if="mode === 'login'">还没有账户？<a href="#" @click.prevent="switchMode('register')">立即注册</a></template>
        <template v-else>已有账户？<a href="#" @click.prevent="switchMode('login')">返回登录</a></template>
      </p>
    </section>
  </main>

  <main v-else-if="page === 'home'" class="buyer-layout">
    <aside class="buyer-sidebar">
      <div class="buyer-logo">
        <img src="/logo.png" alt="零蓝权益" />
        <div>
          <strong>零蓝权益</strong>
          <span>采购管理平台</span>
        </div>
      </div>
      <nav class="buyer-nav" aria-label="采购端导航">
        <button class="active" type="button"><Home :size="20" />主页</button>
        <button type="button"><ShoppingCart :size="20" />采购中心</button>
        <button type="button"><Box :size="20" />订单管理</button>
        <button type="button"><WalletCards :size="20" />财务管理</button>
        <button type="button"><CreditCard :size="20" />卡密中心</button>
        <button type="button"><BarChart3 :size="20" />报表中心</button>
        <button type="button"><Package :size="20" />合同管理</button>
        <button type="button"><UserRound :size="20" />个人中心</button>
      </nav>
    </aside>

    <section class="buyer-main">
      <header class="buyer-topbar">
        <h1>采购工作台</h1>
        <div class="admin-user">
          <button class="icon-button" type="button" aria-label="通知">
            <Bell :size="20" />
            <span></span>
          </button>
          <div class="avatar light"><UserRound :size="18" /></div>
          <strong>{{ currentUsername }}</strong>
        </div>
      </header>

      <div class="buyer-content">
        <section class="quick-section">
          <h2>常用功能</h2>
          <div class="quick-grid">
            <button class="quick-card" type="button" aria-label="权益采购">
              <span class="quick-icon"><HandCoins :size="40" :stroke-width="2.1" /></span>
              <span>权益采购</span>
            </button>
            <button class="quick-card" type="button" aria-label="订单记录">
              <span class="quick-icon"><ReceiptText :size="40" :stroke-width="2.1" /></span>
              <span>订单记录</span>
            </button>
            <button class="quick-card" type="button" aria-label="资金流水">
              <span class="quick-icon"><Banknote :size="40" :stroke-width="2.1" /></span>
              <span>资金流水</span>
            </button>
            <button class="quick-card" type="button" aria-label="卡密管理">
              <span class="quick-icon"><KeyRound :size="40" :stroke-width="2.1" /></span>
              <span>卡密管理</span>
            </button>
            <button class="quick-card" type="button" aria-label="数据报表">
              <span class="quick-icon"><ChartNoAxesCombined :size="40" :stroke-width="2.1" /></span>
              <span>数据报表</span>
            </button>
            <button class="quick-card" type="button" aria-label="合同管理">
              <span class="quick-icon"><FileText :size="40" :stroke-width="2.1" /></span>
              <span>合同管理</span>
            </button>
          </div>
        </section>

        <section class="buyer-grid">
          <article class="buyer-chart-card">
            <h2>采购数据概览</h2>
            <div class="buyer-metrics">
              <span><small>本月采购额（元）</small><strong>￥58,790</strong></span>
              <span><small>交易订单（笔）</small><strong>127</strong></span>
              <span><small>待支付（元）</small><strong>￥12,450</strong></span>
              <span><small>待收货（单）</small><strong>23</strong></span>
            </div>
            <div class="fake-chart">
              <svg viewBox="0 0 900 260" role="img" aria-label="采购趋势">
                <path d="M0 190 C80 185 70 90 150 92 C250 96 250 150 330 142 C430 130 405 55 500 62 C610 70 560 145 650 140 C740 135 700 35 800 40 C850 42 860 82 900 86" />
              </svg>
            </div>
          </article>

          <aside class="buyer-account-card">
            <h2>账户信息</h2>
            <p>账户状态：正常</p>
            <dl>
              <div><dt>授信额度</dt><dd>￥500,000</dd></div>
              <div><dt>当前余额</dt><dd>￥387,210</dd></div>
              <div><dt>冻结保证金</dt><dd>￥50,000</dd></div>
              <div><dt>授信评级</dt><dd>AAA</dd></div>
            </dl>
          </aside>
        </section>
      </div>
    </section>
  </main>

  <main v-else-if="page === 'adminAuth'" class="login-page">
    <section class="login-panel">
      <img class="login-logo" src="/logo.png" alt="零蓝权益" />
      <h1>后台管理系统</h1>
      <p>管理员登录</p>
      <form class="login-form" @submit.prevent="handleAdminSubmit">
        <label>
          <span>管理员账号</span>
          <input v-model="adminUsername" autocomplete="username" placeholder="请输入管理员账号" type="text" />
        </label>
        <label>
          <span>管理员密码</span>
          <input v-model="adminPassword" autocomplete="current-password" placeholder="请输入管理员密码" type="password" />
        </label>
        <p v-if="adminMessage" class="form-message">{{ adminMessage }}</p>
        <button type="submit" :disabled="adminLoading || !adminUsername.trim() || !adminPassword">
          {{ adminLoading ? '登录中...' : '登录后台系统' }}
        </button>
      </form>
    </section>
  </main>

  <main v-else-if="page === 'admin'" class="admin-layout">
    <aside class="admin-sidebar">
      <div class="admin-logo">
        <img src="/logo.png" alt="零蓝校送" />
        <div>
          <strong>零蓝校送</strong>
          <span>后台管理平台</span>
        </div>
      </div>

      <nav class="admin-nav" aria-label="后台导航">
        <div v-for="menu in adminMenus" :key="menu.name" class="admin-nav-group">
          <button
            type="button"
            :class="{ active: menu.view === adminView || (menu.children && isGoodsAdminView()) }"
            @click="menu.view && switchAdminView(menu.view)"
          >
            <component :is="menu.icon" :size="18" />
            <span>{{ menu.name }}</span>
            <ChevronDown v-if="menu.expanded" :size="16" class="nav-arrow" />
            <ChevronRight v-else :size="16" class="nav-arrow" />
          </button>
          <div v-if="menu.children && menu.expanded" class="sub-nav">
            <button
              v-for="(child, childIndex) in menu.children"
              :key="child.name"
              type="button"
              :class="{ active: child.view === adminView }"
              @click="child.view && switchAdminView(child.view)"
            >
              {{ child.name }}
            </button>
          </div>
        </div>
      </nav>
    </aside>

    <section class="admin-main">
      <header class="admin-topbar">
        <h1>后台管理系统</h1>
        <div class="admin-user">
          <button class="icon-button" type="button" aria-label="通知">
            <Bell :size="20" />
            <span></span>
          </button>
          <div class="avatar"><UserRound :size="18" /></div>
          <strong>{{ currentAdminName }}</strong>
        </div>
      </header>

      <div v-if="adminView === 'home'" class="admin-content">
        <section class="page-heading">
          <h2>首页</h2>
          <p>平台运营数据总览</p>
        </section>

        <section class="overview-row">
          <article class="overview-tile">
            <span>平台已上线</span>
            <strong>365 <small>天</small></strong>
          </article>
          <article class="overview-tile">
            <span>总用户数</span>
            <strong>12,458 <small>人</small></strong>
          </article>
          <article class="overview-tile">
            <span>商品总数</span>
            <strong>1,234 <small>件</small></strong>
          </article>
        </section>

        <section class="admin-section">
          <h2>今日数据</h2>
          <div class="admin-stat-grid">
            <article v-for="item in todayStats" :key="item.label" class="admin-stat-card" :class="item.tone">
              <component :is="item.icon" :size="22" />
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </article>
          </div>
        </section>

        <section class="admin-section">
          <h2>本月数据</h2>
          <div class="admin-stat-grid">
            <article v-for="item in monthStats" :key="item.label" class="admin-stat-card" :class="item.tone">
              <component :is="item.icon" :size="22" />
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </article>
          </div>
        </section>
      </div>

      <div v-else-if="adminView === 'category'" class="admin-content">
        <section class="page-heading">
          <h2>商品分类</h2>
          <p>管理商品分类信息</p>
        </section>

        <section class="category-panel">
          <div class="category-toolbar">
            <button class="primary-action" type="button" @click="openCreateModal()">
              <Plus :size="18" />
              <span>添加分类</span>
            </button>
            <button class="soft-action" type="button" :disabled="!selectedIds.length" @click="batchDelete">批量删除</button>
            <p v-if="categoryLoading" class="toolbar-note">正在加载分类...</p>
          </div>

          <p v-if="categoryMessage" class="category-message">{{ categoryMessage }}</p>

          <div class="category-table" role="table" aria-label="商品分类列表">
            <div class="category-row table-head" role="row">
              <label class="check-cell">
                <input type="checkbox" :checked="allVisibleSelected" @change="toggleSelectAll" />
              </label>
              <span></span>
              <span>分类图标</span>
              <span>分类编号</span>
              <span>分类名称</span>
              <span>排序号</span>
              <span>状态</span>
              <span>操作</span>
            </div>

            <div
              v-for="row in visibleRows"
              :key="row.id"
              class="category-row"
              :class="{ child: row.level === 2 }"
              role="row"
            >
              <label class="check-cell">
                <input type="checkbox" :checked="selectedIds.includes(row.id)" @change="toggleSelect(row.id)" />
              </label>

              <button
                v-if="row.level === 1"
                class="expand-button"
                type="button"
                :disabled="!row.children?.length"
                @click="toggleExpand(row.id)"
              >
                <ChevronDown v-if="expandedIds.includes(row.id)" :size="18" />
                <ChevronRight v-else :size="18" />
              </button>
              <span v-else class="child-spacer"></span>

              <span class="category-icon">
                <img v-if="isImageIcon(row.icon)" :src="row.icon || ''" alt="" />
                <span v-else>{{ row.icon || (row.level === 1 ? 'CAT' : 'SUB') }}</span>
              </span>

              <strong class="category-id">{{ row.id }}</strong>
              <span class="category-name" :class="{ child: row.level === 2 }">
                <span v-if="row.level === 2" class="tree-line">--></span>
                {{ row.name }}
              </span>

              <input class="sort-input" :value="row.sort" type="number" min="0" @change="updateSort(row, $event)" />

              <button class="switch" :class="{ on: row.status === 1 }" type="button" @click="changeStatus(row)">
                <span></span>
              </button>

              <span class="row-actions">
                <button class="text-action edit" type="button" @click="openEditModal(row)">
                  <Edit3 :size="14" />
                  编辑
                </button>
                <button class="text-action danger" type="button" @click="deleteCategory(row)">
                  <Trash2 :size="14" />
                  删除
                </button>
              </span>
            </div>

            <div v-if="!visibleRows.length && !categoryLoading" class="empty-row">暂无分类数据</div>
          </div>

          <footer class="category-footer">
            <span>共 {{ totalCategoryCount }} 条记录，已选择 {{ selectedIds.length }} 条</span>
          </footer>
        </section>
      </div>

      <div v-else-if="adminView === 'pricingTemplate'" class="admin-content">
        <section class="page-heading">
          <h2>定价模板</h2>
          <p>维护商品基于渠道成本价的加价规则</p>
        </section>

        <section class="category-panel">
          <div class="category-toolbar pricing-toolbar">
            <button class="primary-action" type="button" @click="openCreatePricingTemplateModal()">
              <Plus :size="18" />
              <span>添加模板</span>
            </button>
            <label>
              <span>模板名称</span>
              <input v-model="pricingTemplateFilters.name" placeholder="输入名称筛选" type="text" @keyup.enter="loadPricingTemplates" />
            </label>
            <label>
              <span>定价方式</span>
              <select v-model="pricingTemplateFilters.pricingType">
                <option value="">全部</option>
                <option value="PERCENTAGE">百分比加价</option>
                <option value="FIXED_AMOUNT">固定金额加价</option>
              </select>
            </label>
            <label>
              <span>状态</span>
              <select v-model="pricingTemplateFilters.status">
                <option value="">全部</option>
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </label>
            <button class="soft-action" type="button" @click="loadPricingTemplates">筛选</button>
            <button class="soft-action" type="button" @click="resetPricingTemplateFilters">重置</button>
            <p v-if="pricingTemplateLoading" class="toolbar-note">正在加载模板...</p>
          </div>

          <p v-if="pricingTemplateMessage" class="category-message">{{ pricingTemplateMessage }}</p>

          <div class="category-table" role="table" aria-label="定价模板列表">
            <div class="pricing-template-row table-head" role="row">
              <span>模板编号</span>
              <span>模板名称</span>
              <span>定价方式</span>
              <span>定价值</span>
              <span>排序号</span>
              <span>状态</span>
              <span>更新时间</span>
              <span>操作</span>
            </div>

            <div v-for="template in pricingTemplates" :key="template.id" class="pricing-template-row" role="row">
              <strong>{{ template.id }}</strong>
              <span class="template-name">
                <strong>{{ template.name }}</strong>
                <small v-if="template.description">{{ template.description }}</small>
              </span>
              <span>{{ pricingTypeLabel(template.pricingType) }}</span>
              <strong class="pricing-value">{{ formatPricingValue(template) }}</strong>
              <span>{{ template.sort }}</span>
              <button class="switch" :class="{ on: template.status === 1 }" type="button" @click="changePricingTemplateStatus(template)">
                <span></span>
              </button>
              <span>{{ formatDateTime(template.updateTime || template.createTime) }}</span>
              <span class="row-actions">
                <button class="text-action edit" type="button" @click="openEditPricingTemplateModal(template)">
                  <Edit3 :size="14" />
                  编辑
                </button>
                <button class="text-action danger" type="button" @click="deletePricingTemplate(template)">
                  <Trash2 :size="14" />
                  删除
                </button>
              </span>
            </div>

            <div v-if="!pricingTemplates.length && !pricingTemplateLoading" class="empty-row">暂无定价模板数据</div>
          </div>

          <footer class="category-footer">
            <span>共 {{ pricingTemplates.length }} 条记录</span>
          </footer>
        </section>
      </div>

      <div v-else-if="adminView === 'mediaAsset'" class="admin-content">
        <section class="page-heading">
          <h2>素材管理</h2>
          <p>管理分类图标和商品图片素材，清理未使用文件</p>
        </section>

        <section class="category-panel">
          <div class="category-toolbar pricing-toolbar">
            <label class="upload-button">
              <input accept=".jpg,.jpeg,.png,.webp,.svg,image/jpeg,image/png,image/webp,image/svg+xml" type="file" :disabled="mediaAssetUploading" @change="uploadMediaAsset($event, 'category')" />
              <span>{{ mediaAssetUploading ? '上传中...' : '上传分类图标' }}</span>
            </label>
            <label class="upload-button">
              <input accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp" type="file" :disabled="mediaAssetUploading" @change="uploadMediaAsset($event, 'product')" />
              <span>{{ mediaAssetUploading ? '上传中...' : '上传商品图片' }}</span>
            </label>
            <label>
              <span>素材类型</span>
              <select v-model="mediaAssetFilters.scene">
                <option value="">全部</option>
                <option value="category">分类图标</option>
                <option value="product">商品图片</option>
              </select>
            </label>
            <label>
              <span>文件名</span>
              <input v-model="mediaAssetFilters.filename" placeholder="输入文件名" type="text" @keyup.enter="loadMediaAssets" />
            </label>
            <label>
              <span>使用状态</span>
              <select v-model="mediaAssetFilters.used">
                <option value="">全部</option>
                <option value="true">使用中</option>
                <option value="false">未使用</option>
              </select>
            </label>
            <button class="soft-action" type="button" @click="loadMediaAssets">筛选</button>
            <button class="soft-action" type="button" @click="resetMediaAssetFilters">重置</button>
            <p v-if="mediaAssetLoading" class="toolbar-note">正在加载素材...</p>
          </div>

          <p v-if="mediaAssetMessage" class="category-message">{{ mediaAssetMessage }}</p>

          <div class="media-grid">
            <article v-for="asset in mediaAssets" :key="asset.id" class="media-card">
              <div class="media-thumb">
                <img :src="asset.url" :alt="asset.originalName || asset.filename" />
              </div>
              <div class="media-meta">
                <strong>{{ asset.originalName || asset.filename }}</strong>
                <span>{{ sceneLabel(asset.scene) }} · {{ formatFileSize(asset.size) }}</span>
                <span>{{ asset.width && asset.height ? `${asset.width} x ${asset.height}` : asset.extension.toUpperCase() }}</span>
                <span :class="asset.used ? 'used-tag' : 'unused-tag'">{{ asset.used ? '使用中' : '未使用' }}</span>
              </div>
              <footer>
                <button class="text-action edit" type="button" @click="copyMediaAssetUrl(asset)">复制地址</button>
                <button class="text-action danger" type="button" :disabled="asset.used" @click="deleteMediaAsset(asset)">
                  <Trash2 :size="14" />
                  删除
                </button>
              </footer>
            </article>

            <div v-if="!mediaAssets.length && !mediaAssetLoading" class="empty-row">暂无素材数据</div>
          </div>
        </section>
      </div>
    </section>

    <div v-if="modalOpen" class="modal-mask" role="dialog" aria-modal="true">
      <form class="category-modal" @submit.prevent="saveCategory">
        <header>
          <h2>{{ modalMode === 'create' ? '添加分类' : '编辑分类' }}</h2>
          <button type="button" aria-label="关闭" @click="modalOpen = false"><X :size="18" /></button>
        </header>

        <label>
          <span>上级分类</span>
          <select v-model.number="categoryForm.parentId">
            <option :value="0">一级分类</option>
            <option v-for="item in parentOptions" :key="item.id" :value="item.id" :disabled="item.id === categoryForm.id">
              {{ item.name }}
            </option>
          </select>
        </label>

        <label>
          <span>分类名称</span>
          <input v-model="categoryForm.name" maxlength="50" placeholder="请输入分类名称" />
        </label>

        <label>
          <span>分类图标</span>
          <input v-model="categoryForm.icon" placeholder="可填文字缩写、图片 URL 或上传图片" />
        </label>

        <div class="icon-upload-field">
          <span class="category-icon preview">
            <img v-if="isImageIcon(categoryForm.icon)" :src="categoryForm.icon" alt="" />
            <span v-else>{{ categoryForm.icon || 'CAT' }}</span>
          </span>
          <label class="upload-button">
            <input
              accept=".jpg,.jpeg,.png,.webp,.svg,image/jpeg,image/png,image/webp,image/svg+xml"
              type="file"
              :disabled="categoryIconUploading"
              @change="uploadCategoryIcon"
            />
            <span>{{ categoryIconUploading ? '上传中...' : '上传图片' }}</span>
          </label>
          <button class="soft-action" type="button" :disabled="categoryIconUploading" @click="openMediaAssetPicker">
            选择素材
          </button>
          <button class="soft-action" type="button" :disabled="categoryIconUploading || !categoryForm.icon" @click="categoryForm.icon = ''">
            清空
          </button>
        </div>

        <label v-if="modalMode === 'edit'">
          <span>排序号</span>
          <input v-model.number="categoryForm.sort" min="0" type="number" />
        </label>

        <label>
          <span>状态</span>
          <select v-model.number="categoryForm.status">
            <option :value="1">启用</option>
            <option :value="0">禁用</option>
          </select>
        </label>

        <footer>
          <button class="soft-action" type="button" @click="modalOpen = false">取消</button>
          <button class="primary-action" type="submit" :disabled="savingCategory || !categoryForm.name.trim()">
            {{ savingCategory ? '保存中...' : '保存' }}
          </button>
        </footer>
      </form>
    </div>

    <div v-if="mediaAssetPickerOpen" class="modal-mask" role="dialog" aria-modal="true">
      <section class="media-picker">
        <header>
          <h2>选择素材</h2>
          <button type="button" aria-label="关闭" @click="mediaAssetPickerOpen = false"><X :size="18" /></button>
        </header>

        <div class="media-picker-toolbar">
          <label>
            <span>文件名</span>
            <input v-model="mediaAssetFilters.filename" placeholder="搜索素材" type="text" @keyup.enter="loadMediaAssets" />
          </label>
          <button class="soft-action" type="button" @click="loadMediaAssets">搜索</button>
        </div>

        <div class="media-grid picker">
          <button v-for="asset in mediaAssets" :key="asset.id" class="media-card pickable" type="button" @click="selectMediaAsset(asset)">
            <span class="media-thumb">
              <img :src="asset.url" :alt="asset.originalName || asset.filename" />
            </span>
            <span class="media-meta">
              <strong>{{ asset.originalName || asset.filename }}</strong>
              <small>{{ formatFileSize(asset.size) }}</small>
            </span>
          </button>
          <div v-if="!mediaAssets.length && !mediaAssetLoading" class="empty-row">暂无可选素材</div>
        </div>
      </section>
    </div>

    <div v-if="pricingTemplateModalOpen" class="modal-mask" role="dialog" aria-modal="true">
      <form class="category-modal pricing-template-modal" @submit.prevent="savePricingTemplate">
        <header>
          <h2>{{ pricingTemplateModalMode === 'create' ? '添加定价模板' : '编辑定价模板' }}</h2>
          <button type="button" aria-label="关闭" @click="pricingTemplateModalOpen = false"><X :size="18" /></button>
        </header>

        <label>
          <span>模板名称</span>
          <input v-model="pricingTemplateForm.name" maxlength="50" placeholder="请输入模板名称" type="text" />
        </label>

        <label>
          <span>定价方式</span>
          <select v-model="pricingTemplateForm.pricingType">
            <option value="PERCENTAGE">百分比加价</option>
            <option value="FIXED_AMOUNT">固定金额加价</option>
          </select>
        </label>

        <label>
          <span>{{ pricingTemplateForm.pricingType === 'PERCENTAGE' ? '加价百分比' : '固定加价金额' }}</span>
          <input v-model.number="pricingTemplateForm.pricingValue" min="0" step="0.0001" type="number" />
        </label>

        <label>
          <span>排序号</span>
          <input v-model.number="pricingTemplateForm.sort" min="0" type="number" />
        </label>

        <label>
          <span>状态</span>
          <select v-model.number="pricingTemplateForm.status">
            <option :value="1">启用</option>
            <option :value="0">禁用</option>
          </select>
        </label>

        <label>
          <span>备注</span>
          <textarea v-model="pricingTemplateForm.description" maxlength="255" placeholder="可填写模板适用说明"></textarea>
        </label>

        <footer>
          <button class="soft-action" type="button" @click="pricingTemplateModalOpen = false">取消</button>
          <button class="primary-action" type="submit" :disabled="savingPricingTemplate || !pricingTemplateForm.name.trim()">
            {{ savingPricingTemplate ? '保存中...' : '保存' }}
          </button>
        </footer>
      </form>
    </div>
  </main>

  <main v-else class="front-page">
    <section class="front-card">
      <img src="/logo.png" alt="零蓝校送" />
      <p class="front-eyebrow">权益商品服务平台</p>
      <h1>零蓝校送</h1>
      <p class="front-copy">面向用户的商品浏览、权益采购和订单服务入口。</p>
      <div class="front-actions">
        <button class="primary-action" type="button" @click="navigate('/home')">进入前台</button>
        <button class="soft-action" type="button" @click="navigate('/admin/index')">后台管理</button>
      </div>
    </section>
  </main>
</template>
