<script setup lang="ts">
import { computed, onMounted, reactive, ref, type Component } from 'vue'
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
  Search,
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
type AdminView = 'home' | 'product' | 'category' | 'pricingTemplate' | 'orderTemplate' | 'mediaAsset' | 'supplyChannel' | 'user' | 'order'
type BuyerView = 'dashboard' | 'benefits' | 'products' | 'orders'
type AuthMode = 'login' | 'register'
type PricingType = 'PERCENTAGE' | 'FIXED_AMOUNT'
type ProductType = 'VIRTUAL' | 'CARD' | 'NORMAL'
type SupplyCostStrategy = 'LOWEST' | 'HIGHEST'
type OrderFieldType = 'PHONE' | 'QQ' | 'EMAIL' | 'ADDRESS' | 'TEXT'

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

type UserProfileResponse = {
  userId: number
  username: string
  balance: number
}

type AdminUserResponse = {
  id: number
  username: string
  email?: string | null
  phone?: string | null
  nickname?: string | null
  avatar?: string | null
  balance: number
  status: number
  registerIp?: string | null
  registerTime?: string
  lastLoginTime?: string
  updateTime?: string
}

type AdminUserFilters = {
  keyword: string
  status: '' | 0 | 1
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
  description?: string | null
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
  description: string
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

type OrderTemplateFieldResponse = {
  id?: number
  templateId?: number
  fieldType: OrderFieldType
  fieldName: string
  placeholder?: string | null
  required: boolean
  sort: number
}

type OrderTemplateResponse = {
  id: number
  name: string
  description?: string | null
  sort: number
  status: number
  fields: OrderTemplateFieldResponse[]
  createTime?: string
  updateTime?: string
}

type OrderTemplateForm = {
  id?: number
  name: string
  description: string
  sort: number
  status: number
  fields: OrderTemplateFieldResponse[]
}

type OrderTemplateFilters = {
  name: string
  status: '' | 0 | 1
}

type ProductResponse = {
  id: number
  productType: ProductType
  categoryId: number
  categoryName?: string | null
  name: string
  costPrice: number
  salePrice: number
  terminalLimitPrice?: number | null
  supplyCostStrategy?: SupplyCostStrategy | null
  pricingTemplateId: number
  pricingTemplateName?: string | null
  image?: string | null
  faceValue?: number | null
  orderTemplateId: number
  orderTemplateName?: string | null
  minPurchaseQuantity: number
  maxPurchaseQuantity?: number | null
  sort: number
  status: number
  supplyBindings?: ProductSupplyBindingResponse[]
  createTime?: string
  updateTime?: string
}

type SupplyChannelResponse = {
  id: number
  channelType: 'YOUKAYUN'
  channelTypeName: string
  name: string
  apiUrl: string
  userId: string
  secretKey: string
  sort: number
  status: number
  createTime?: string
  updateTime?: string
}

type SupplyChannelBalanceResponse = {
  channelId: number
  channelType: string
  balance?: number | string | null
  message?: string
}

type SupplyChannelProductResponse = {
  channelId: number
  channelType: string
  channelProductId: string
  channelProductName?: string | null
  channelCostPrice?: number | string | null
  message?: string
}

type SupplyChannelForm = {
  id?: number
  channelType: 'YOUKAYUN'
  name: string
  apiUrl: string
  userId: string
  secretKey: string
  sort: number
  status: number
}

type SupplyChannelFilters = {
  name: string
  channelType: '' | 'YOUKAYUN'
  status: '' | 0 | 1
}

type ProductSupplyBindingResponse = {
  id?: number
  productId?: number
  channelId: number | ''
  channelName?: string | null
  channelType?: string | null
  channelProductId: string
  channelProductName: string
  channelCostPrice: number
  active?: boolean
  sort: number
  status: number
}

type VirtualOrderStatus = 'PENDING' | 'PROCESSING' | 'SUCCESS' | 'REFUNDED' | 'EXCEPTION'

type VirtualOrderResponse = {
  id: number
  orderNo: string
  userId: number
  username: string
  productId: number
  productName: string
  productSnapshot?: string | null
  quantity: number
  rechargeAccount: string
  orderAmount: number
  paymentMethod: string
  sourceIp?: string | null
  status: VirtualOrderStatus
  channelId?: number | null
  channelName?: string | null
  channelType?: string | null
  channelOrderNo?: string | null
  exceptionMessage?: string | null
  createdAt?: string
  processedAt?: string | null
  processingDurationSeconds?: number | null
  updateTime?: string
}

type OrderFilters = {
  orderNo: string
  status: '' | VirtualOrderStatus
  productName: string
  rechargeAccount: string
  paymentMethod: '' | 'BALANCE'
}

type BuyerOrderFilters = OrderFilters & {
  productId: string
  channelOrderNo: string
  startTime: string
  endTime: string
}

type ProductForm = {
  id?: number
  productType: ProductType
  categoryId: number | ''
  name: string
  costPrice: number
  terminalLimitPrice: number | null
  supplyCostStrategy: SupplyCostStrategy
  pricingTemplateId: number | ''
  image: string
  faceValue: number | null
  orderTemplateId: number | ''
  minPurchaseQuantity: number
  maxPurchaseQuantity: number | null
  sort: number
  status: number
  supplyBindings: ProductSupplyBindingResponse[]
}

type ProductFilters = {
  name: string
  productType: '' | ProductType
  categoryId: number | ''
  status: '' | 0 | 1
}

type BuyerProductStatus = '' | 0 | 1
type AdminMenu = {
  name: string
  icon: Component
  view?: AdminView
  expanded?: boolean
  children?: { name: string; view: AdminView }[]
}

const currentPath = ref(window.location.pathname)
const adminView = ref<AdminView>(getAdminViewFromPath(currentPath.value))
const buyerView = ref<BuyerView>('benefits')

const page = computed<Page>(() => {
  const path = normalizePath(currentPath.value)
  if (path === '/login') return 'userAuth'
  if (path === '/admin/login') return 'adminAuth'
  if (
    path === '/admin' ||
    path === '/admin/index' ||
    path === '/admin/goods/products' ||
    path === '/admin/goods/category' ||
    path === '/admin/goods/pricing-template' ||
    path === '/admin/goods/order-templates' ||
    path === '/admin/goods/media-assets' ||
    path === '/admin/goods/supply-channels' ||
    path === '/admin/users' ||
    path === '/admin/orders'
  ) return 'admin'
  if (
    path === '/home' ||
    path === '/index' ||
    path === '/purchase' ||
    path === '/purchase/benefits' ||
    path === '/purchase/products' ||
    path === '/purchase/orders' ||
    path === '/purchase/order' ||
    path === '/orders' ||
    path === '/order'
  ) return 'home'
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
  description: '',
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
const orderTemplates = ref<OrderTemplateResponse[]>([])
const orderTemplateLoading = ref(false)
const orderTemplateMessage = ref('')
const savingOrderTemplate = ref(false)
const orderTemplateModalOpen = ref(false)
const orderTemplateModalMode = ref<'create' | 'edit'>('create')
const orderTemplateFilters = reactive<OrderTemplateFilters>({
  name: '',
  status: '',
})
const orderTemplateForm = reactive<OrderTemplateForm>({
  name: '',
  description: '',
  sort: 0,
  status: 1,
  fields: [],
})
const products = ref<ProductResponse[]>([])
const productLoading = ref(false)
const productMessage = ref('')
const savingProduct = ref(false)
const productImageUploading = ref(false)
const productModalOpen = ref(false)
const productModalMode = ref<'create' | 'edit'>('create')
const productModalTab = ref<'basic' | 'supply'>('basic')
const syncingSupplyBindingIndex = ref<number | null>(null)
const productFilters = reactive<ProductFilters>({
  name: '',
  productType: '',
  categoryId: '',
  status: '',
})
const productForm = reactive<ProductForm>({
  productType: 'VIRTUAL',
  categoryId: '',
  name: '',
  costPrice: 0,
  terminalLimitPrice: null,
  supplyCostStrategy: 'LOWEST',
  pricingTemplateId: '',
  image: '',
  faceValue: null,
  orderTemplateId: '',
  minPurchaseQuantity: 1,
  maxPurchaseQuantity: null,
  sort: 0,
  status: 1,
  supplyBindings: [],
})
const supplyChannels = ref<SupplyChannelResponse[]>([])
const supplyChannelLoading = ref(false)
const supplyChannelMessage = ref('')
const supplyChannelBalances = reactive<Record<number, string>>({})
const queryingSupplyChannelId = ref<number | null>(null)
const savingSupplyChannel = ref(false)
const supplyChannelModalOpen = ref(false)
const supplyChannelModalMode = ref<'create' | 'edit'>('create')
const supplyChannelFilters = reactive<SupplyChannelFilters>({
  name: '',
  channelType: '',
  status: '',
})
const supplyChannelForm = reactive<SupplyChannelForm>({
  channelType: 'YOUKAYUN',
  name: '',
  apiUrl: '',
  userId: '',
  secretKey: '',
  sort: 0,
  status: 1,
})
const mediaAssets = ref<MediaAssetResponse[]>([])
const mediaAssetLoading = ref(false)
const mediaAssetMessage = ref('')
const mediaAssetUploading = ref(false)
const mediaAssetPickerOpen = ref(false)
const mediaAssetPickerTarget = ref<'category' | 'product'>('category')
const mediaAssetFilters = reactive<MediaAssetFilters>({
  scene: '',
  filename: '',
  used: '',
})
const buyerCategories = ref<CategoryTreeResponse[]>([])
const buyerProducts = ref<ProductResponse[]>([])
const buyerStoreLoading = ref(false)
const buyerStoreMessage = ref('')
const buyerSearch = ref('')
const buyerCategoryId = ref<number | ''>('')
const buyerProductStatus = ref<BuyerProductStatus>('')
const buyerProductPage = ref(1)
const buyerProductPageSize = ref(10)
const buyerCategoryModalOpen = ref(false)
const viewingBuyerCategory = ref<CategoryResponse | CategoryTreeResponse | null>(null)
const buyerCategoryModalPosition = reactive({ x: 0, y: 0 })
const buyerCategoryModalDragStart = reactive({ pointerX: 0, pointerY: 0, x: 0, y: 0 })
const buyerCategoryModalDragging = ref(false)
const buyerCategoryPickerOpen = ref(false)
const activeBuyerParentCategoryId = ref<number | null>(null)
const currentBalance = ref(0)
const purchaseModalOpen = ref(false)
const purchasingProduct = ref<ProductResponse | null>(null)
const purchaseRechargeAccount = ref('')
const purchaseQuantity = ref(1)
const purchasePaymentMethod = ref<'BALANCE'>('BALANCE')
const purchaseConfirmed = ref(false)
const purchaseSubmitting = ref(false)
const purchaseMessage = ref('')
const buyerOrders = ref<VirtualOrderResponse[]>([])
const buyerOrderLoading = ref(false)
const buyerOrderMessage = ref('')
const buyerOrderFilters = reactive<BuyerOrderFilters>({
  rechargeAccount: '',
  productName: '',
  productId: '',
  orderNo: '',
  channelOrderNo: '',
  status: '',
  paymentMethod: '',
  startTime: '',
  endTime: '',
})
const adminOrders = ref<VirtualOrderResponse[]>([])
const adminOrderLoading = ref(false)
const adminOrderMessage = ref('')
const adminOrderFilters = reactive<OrderFilters>({
  orderNo: '',
  status: '',
  productName: '',
  rechargeAccount: '',
  paymentMethod: '',
})
const adminUsers = ref<AdminUserResponse[]>([])
const adminUserLoading = ref(false)
const adminUserMessage = ref('')
const adminUserFilters = reactive<AdminUserFilters>({
  keyword: '',
  status: '',
})

const adminMenus = reactive<AdminMenu[]>([
  { name: '首页', icon: Home, view: 'home' as AdminView },
  {
    name: '商品',
    icon: Package,
    expanded: true,
    children: [
      { name: '商品管理', view: 'product' as AdminView },
      { name: '商品分类', view: 'category' as AdminView },
      { name: '定价模板', view: 'pricingTemplate' as AdminView },
      { name: '下单模板', view: 'orderTemplate' as AdminView },
      { name: '素材管理', view: 'mediaAsset' as AdminView },
      { name: '货源渠道', view: 'supplyChannel' as AdminView },
    ],
  },
  { name: '用户', icon: Users, view: 'user' as AdminView },
  { name: '订单', icon: ShoppingCart, view: 'order' as AdminView },
  { name: '数据', icon: BarChart3 },
  { name: '运营', icon: Megaphone },
  { name: '设置', icon: Settings },
])

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
const flatCategoryOptions = computed(() => {
  const options: { id: number; name: string }[] = []
  for (const category of categoryTree.value) {
    options.push({ id: category.id, name: category.name })
    for (const child of category.children || []) {
      options.push({ id: child.id, name: `${category.name} / ${child.name}` })
    }
  }
  return options
})
const productSalePricePreview = computed(() => {
  const template = pricingTemplates.value.find((item) => item.id === Number(productForm.pricingTemplateId))
  if (!template) return null
  const cost = Number(productForm.costPrice)
  if (Number.isNaN(cost) || cost < 0) return null
  if (template.pricingType === 'PERCENTAGE') {
    return cost * (1 + Number(template.pricingValue) / 100)
  }
  return cost + Number(template.pricingValue)
})
const buyerCategoryTabs = computed(() => [
  { id: '' as const, name: '全部权益' },
  ...buyerCategories.value.map((category) => ({ id: category.id, name: category.name })),
])
const filteredBuyerProducts = computed(() => {
  const keyword = buyerSearch.value.trim().toLowerCase()
  const categoryIds = resolveBuyerCategoryIds(buyerCategoryId.value)
  return buyerProducts.value.filter((product) => {
    const matchesKeyword =
      !keyword ||
      product.name.toLowerCase().includes(keyword) ||
      (product.categoryName || '').toLowerCase().includes(keyword)
    const matchesCategory = buyerCategoryId.value === '' || categoryIds.includes(product.categoryId)
    const matchesStatus = buyerProductStatus.value === '' || product.status === buyerProductStatus.value
    return matchesKeyword && matchesCategory && matchesStatus
  })
})
const buyerProductsPageCount = computed(() => Math.max(1, Math.ceil(filteredBuyerProducts.value.length / buyerProductPageSize.value)))
const pagedBuyerProducts = computed(() => {
  const page = Math.min(buyerProductPage.value, buyerProductsPageCount.value)
  const start = (page - 1) * buyerProductPageSize.value
  return filteredBuyerProducts.value.slice(start, start + buyerProductPageSize.value)
})
const visibleBuyerCategories = computed(() => {
  const keyword = buyerSearch.value.trim().toLowerCase()
  const categoryGroups =
    buyerCategoryId.value === ''
      ? buyerCategories.value
      : buyerCategories.value.filter((category) => category.id === buyerCategoryId.value)
  const categories = categoryGroups.flatMap((category) =>
    category.children?.length ? category.children : [category],
  )
  return categories.filter((category) => !keyword || category.name.toLowerCase().includes(keyword))
})
const viewingCategoryProducts = computed(() =>
  viewingBuyerCategory.value ? productsInBuyerCategory(viewingBuyerCategory.value) : [],
)
const activeBuyerParentCategory = computed(() => {
  if (activeBuyerParentCategoryId.value == null) return buyerCategories.value[0] || null
  return buyerCategories.value.find((category) => category.id === activeBuyerParentCategoryId.value) || null
})
const buyerCategoryPickerLabel = computed(() => {
  if (buyerCategoryId.value === '') return '请选择商品分类'
  for (const category of buyerCategories.value) {
    if (category.id === buyerCategoryId.value) return category.name
    const child = category.children?.find((item) => item.id === buyerCategoryId.value)
    if (child) return `${category.name} / ${child.name}`
  }
  return '请选择商品分类'
})
const purchaseTotalAmount = computed(() =>
  purchasingProduct.value ? Number(purchasingProduct.value.salePrice) * Number(purchaseQuantity.value || 0) * purchaseAccounts.value.length : 0,
)
const purchaseAccounts = computed(() =>
  purchaseRechargeAccount.value
    .split(/\r?\n/)
    .map((account) => account.trim())
    .filter(Boolean),
)
const canSubmitPurchase = computed(() =>
  Boolean(
    purchasingProduct.value &&
      purchaseAccounts.value.length > 0 &&
      purchaseConfirmed.value &&
      purchaseQuantity.value >= (purchasingProduct.value.minPurchaseQuantity || 1) &&
      (purchasingProduct.value.maxPurchaseQuantity == null || purchaseQuantity.value <= purchasingProduct.value.maxPurchaseQuantity) &&
      !purchaseSubmitting.value,
  ),
)

onMounted(() => {
  normalizeRoute()
  syncAdminViewFromPath()
  syncBuyerView()
})

function normalizePath(path: string) {
  if (path.length > 1 && path.endsWith('/')) {
    return path.slice(0, -1)
  }
  return path
}

function normalizeRoute() {
  const path = normalizePath(currentPath.value)
  if (path !== currentPath.value) {
    window.history.replaceState({}, '', path)
    currentPath.value = path
  }
  if (path === '/') {
    window.history.replaceState({}, '', '/login')
    currentPath.value = '/login'
  }
  if (path === '/admin') {
    window.history.replaceState({}, '', '/admin/login')
    currentPath.value = '/admin/login'
  }
  if (path === '/orders' || path === '/order' || path === '/purchase/order') {
    window.history.replaceState({}, '', '/purchase/orders')
    currentPath.value = '/purchase/orders'
  }
}

window.addEventListener('popstate', () => {
  currentPath.value = window.location.pathname
  normalizeRoute()
  syncAdminViewFromPath()
  syncBuyerView()
})

function navigate(path: string) {
  window.history.pushState({}, '', path)
  currentPath.value = path
  normalizeRoute()
  syncAdminViewFromPath()
  syncBuyerView()
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

function handleAdminMenuClick(menu: AdminMenu) {
  if (menu.children?.length) {
    menu.expanded = !menu.expanded
    return
  }
  if (menu.view) {
    switchAdminView(menu.view)
  }
}

function isAdminMenuActive(menu: AdminMenu) {
  return menu.view === adminView.value || Boolean(menu.children?.some((child) => child.view === adminView.value))
}

function getAdminPath(view: AdminView) {
  if (view === 'product') return '/admin/goods/products'
  if (view === 'category') return '/admin/goods/category'
  if (view === 'pricingTemplate') return '/admin/goods/pricing-template'
  if (view === 'orderTemplate') return '/admin/goods/order-templates'
  if (view === 'mediaAsset') return '/admin/goods/media-assets'
  if (view === 'supplyChannel') return '/admin/goods/supply-channels'
  if (view === 'user') return '/admin/users'
  if (view === 'order') return '/admin/orders'
  return '/admin/index'
}

function getAdminViewFromPath(path: string): AdminView {
  if (path === '/admin/goods/products') return 'product'
  if (path === '/admin/goods/category') return 'category'
  if (path === '/admin/goods/pricing-template') return 'pricingTemplate'
  if (path === '/admin/goods/order-templates') return 'orderTemplate'
  if (path === '/admin/goods/media-assets') return 'mediaAsset'
  if (path === '/admin/goods/supply-channels') return 'supplyChannel'
  if (path === '/admin/users') return 'user'
  if (path === '/admin/orders') return 'order'
  return 'home'
}

function syncAdminViewFromPath() {
  if (page.value !== 'admin') return
  adminView.value = getAdminViewFromPath(currentPath.value)
  if (adminView.value === 'category' && categoryTree.value.length === 0) {
    loadCategories()
  }
  if (adminView.value === 'product' && products.value.length === 0) {
    loadProducts()
  }
  if (adminView.value === 'pricingTemplate' && pricingTemplates.value.length === 0) {
    loadPricingTemplates()
  }
  if (adminView.value === 'orderTemplate' && orderTemplates.value.length === 0) {
    loadOrderTemplates()
  }
  if (adminView.value === 'mediaAsset' && mediaAssets.value.length === 0) {
    loadMediaAssets()
  }
  if (adminView.value === 'supplyChannel' && supplyChannels.value.length === 0) {
    loadSupplyChannels()
  }
  if (adminView.value === 'user' && adminUsers.value.length === 0) {
    loadAdminUsers()
  }
  if (adminView.value === 'order' && adminOrders.value.length === 0) {
    loadAdminOrders()
  }
}

function isGoodsAdminView() {
  return ['product', 'category', 'pricingTemplate', 'orderTemplate', 'mediaAsset', 'supplyChannel'].includes(adminView.value)
}

function syncBuyerView() {
  if (page.value !== 'home') return
  buyerView.value = getBuyerViewFromPath(currentPath.value)
  if (!buyerProducts.value.length && !buyerStoreLoading.value) {
    loadBuyerStoreData()
  }
  if (buyerView.value === 'orders' && !buyerOrders.value.length && !buyerOrderLoading.value) {
    loadBuyerOrders()
  }
  if (currentBalance.value === 0) {
    loadCurrentUserProfile()
  }
}

function switchBuyerView(view: BuyerView) {
  navigate(getBuyerPath(view))
}

function getBuyerPath(view: BuyerView) {
  if (view === 'benefits') return '/purchase/benefits'
  if (view === 'products') return '/purchase/products'
  if (view === 'orders') return '/purchase/orders'
  return '/index'
}

function getBuyerViewFromPath(path: string): BuyerView {
  const normalized = normalizePath(path)
  if (normalized === '/purchase' || normalized === '/purchase/benefits') return 'benefits'
  if (normalized === '/purchase/products') return 'products'
  if (normalized === '/purchase/orders' || normalized === '/purchase/order' || normalized === '/orders' || normalized === '/order') return 'orders'
  return 'dashboard'
}

function selectBuyerCategory(id: number | '') {
  buyerCategoryId.value = id
  buyerProductPage.value = 1
}

function toggleBuyerCategoryPicker() {
  buyerCategoryPickerOpen.value = !buyerCategoryPickerOpen.value
  if (buyerCategoryPickerOpen.value && activeBuyerParentCategoryId.value == null) {
    activeBuyerParentCategoryId.value = buyerCategories.value[0]?.id || null
  }
}

function selectBuyerParentCategory(category: CategoryTreeResponse) {
  activeBuyerParentCategoryId.value = category.id
  if (!category.children?.length) {
    selectBuyerCategory(category.id)
    buyerCategoryPickerOpen.value = false
  }
}

function selectBuyerChildCategory(category: CategoryResponse) {
  selectBuyerCategory(category.id)
  buyerCategoryPickerOpen.value = false
}

function resetBuyerProductFilters() {
  buyerSearch.value = ''
  buyerCategoryId.value = ''
  buyerProductStatus.value = ''
  buyerProductPage.value = 1
  buyerCategoryPickerOpen.value = false
}

function updateBuyerSearch(value: string) {
  buyerSearch.value = value
  buyerProductPage.value = 1
}

function updateBuyerProductStatus(value: BuyerProductStatus) {
  buyerProductStatus.value = value
  buyerProductPage.value = 1
}

function updateBuyerProductStatusFromValue(value: string) {
  updateBuyerProductStatus(value === '' ? '' : (Number(value) as 0 | 1))
}

function updateBuyerPageSize(value: number) {
  buyerProductPageSize.value = value
  buyerProductPage.value = 1
}

function formatPurchaseQuantityRange(product: ProductResponse) {
  return `${product.minPurchaseQuantity || 1} - ${product.maxPurchaseQuantity || '不限'}`
}

function productRuleTemplateLabel(product: ProductResponse) {
  return product.orderTemplateName || '默认规则'
}

function openBuyerCategoryProducts(category: CategoryResponse | CategoryTreeResponse) {
  viewingBuyerCategory.value = category
  buyerCategoryModalPosition.x = 0
  buyerCategoryModalPosition.y = 0
  buyerCategoryModalOpen.value = true
}

function clampBuyerCategoryModalPosition(x: number, y: number) {
  const maxX = Math.max(120, window.innerWidth / 2 - 180)
  const maxY = Math.max(80, window.innerHeight / 2 - 110)
  return {
    x: Math.min(maxX, Math.max(-maxX, x)),
    y: Math.min(maxY, Math.max(-maxY, y)),
  }
}

function dragBuyerCategoryModal(event: PointerEvent) {
  if (!buyerCategoryModalDragging.value) return
  const next = clampBuyerCategoryModalPosition(
    buyerCategoryModalDragStart.x + event.clientX - buyerCategoryModalDragStart.pointerX,
    buyerCategoryModalDragStart.y + event.clientY - buyerCategoryModalDragStart.pointerY,
  )
  buyerCategoryModalPosition.x = next.x
  buyerCategoryModalPosition.y = next.y
}

function stopBuyerCategoryModalDrag() {
  buyerCategoryModalDragging.value = false
  window.removeEventListener('pointermove', dragBuyerCategoryModal)
  window.removeEventListener('pointerup', stopBuyerCategoryModalDrag)
}

function startBuyerCategoryModalDrag(event: PointerEvent) {
  if (event.button !== 0) return
  buyerCategoryModalDragging.value = true
  buyerCategoryModalDragStart.pointerX = event.clientX
  buyerCategoryModalDragStart.pointerY = event.clientY
  buyerCategoryModalDragStart.x = buyerCategoryModalPosition.x
  buyerCategoryModalDragStart.y = buyerCategoryModalPosition.y
  window.addEventListener('pointermove', dragBuyerCategoryModal)
  window.addEventListener('pointerup', stopBuyerCategoryModalDrag)
  event.preventDefault()
}

function productsInBuyerCategory(category: CategoryResponse | CategoryTreeResponse) {
  const categoryIds = resolveBuyerCategoryIds(category.id)
  return buyerProducts.value.filter((product) => categoryIds.includes(product.categoryId))
}

function buyerCategoryProductCount(category: CategoryResponse | CategoryTreeResponse) {
  return productsInBuyerCategory(category).length
}

async function loadCurrentUserProfile() {
  try {
    const result = await requestUserJson<UserProfileResponse>('/api/users/me')
    currentBalance.value = Number(result.data.balance || 0)
    currentUsername.value = result.data.username || currentUsername.value
  } catch {
    currentBalance.value = 0
  }
}

function openPurchaseModal(product: ProductResponse) {
  purchasingProduct.value = product
  purchaseQuantity.value = product.minPurchaseQuantity || 1
  purchaseRechargeAccount.value = ''
  purchasePaymentMethod.value = 'BALANCE'
  purchaseConfirmed.value = false
  purchaseMessage.value = ''
  purchaseModalOpen.value = true
  loadCurrentUserProfile()
}

function normalizePurchaseQuantity() {
  if (!purchasingProduct.value) return
  const minQuantity = purchasingProduct.value.minPurchaseQuantity || 1
  const maxQuantity = purchasingProduct.value.maxPurchaseQuantity || minQuantity
  const quantity = Number(purchaseQuantity.value)
  if (!Number.isFinite(quantity) || quantity < minQuantity) {
    purchaseQuantity.value = minQuantity
    return
  }
  if (quantity > maxQuantity) {
    purchaseQuantity.value = maxQuantity
    return
  }
  purchaseQuantity.value = Math.floor(quantity)
}

function isFixedPurchaseQuantity(product: ProductResponse) {
  return product.maxPurchaseQuantity != null && product.maxPurchaseQuantity === (product.minPurchaseQuantity || 1)
}

async function submitPurchase() {
  if (!purchasingProduct.value || !canSubmitPurchase.value) return
  purchaseSubmitting.value = true
  purchaseMessage.value = ''
  try {
    const result = await requestUserJson<VirtualOrderResponse[]>('/api/orders', {
      method: 'POST',
      body: {
        productId: purchasingProduct.value.id,
        quantity: Number(purchaseQuantity.value),
        rechargeAccount: purchaseAccounts.value.join('\n'),
        paymentMethod: purchasePaymentMethod.value,
      },
    })
    purchaseMessage.value = `下单成功，共创建 ${result.data.length} 个订单`
    purchaseModalOpen.value = false
    await Promise.all([loadCurrentUserProfile(), loadBuyerOrders()])
  } catch (error) {
    purchaseMessage.value = error instanceof Error ? error.message : '下单失败'
  } finally {
    purchaseSubmitting.value = false
  }
}

async function loadBuyerOrders() {
  buyerOrderLoading.value = true
  buyerOrderMessage.value = ''
  try {
    const params = new URLSearchParams()
    if (buyerOrderFilters.rechargeAccount.trim()) params.set('rechargeAccount', buyerOrderFilters.rechargeAccount.trim())
    if (buyerOrderFilters.productName.trim()) params.set('productName', buyerOrderFilters.productName.trim())
    if (buyerOrderFilters.productId.trim()) params.set('productId', buyerOrderFilters.productId.trim())
    if (buyerOrderFilters.orderNo.trim()) params.set('orderNo', buyerOrderFilters.orderNo.trim())
    if (buyerOrderFilters.channelOrderNo.trim()) params.set('channelOrderNo', buyerOrderFilters.channelOrderNo.trim())
    if (buyerOrderFilters.status) params.set('status', buyerOrderFilters.status)
    if (buyerOrderFilters.startTime) params.set('startTime', buyerOrderFilters.startTime)
    if (buyerOrderFilters.endTime) params.set('endTime', buyerOrderFilters.endTime)
    const query = params.toString()
    const result = await requestUserJson<VirtualOrderResponse[]>(`/api/orders${query ? `?${query}` : ''}`)
    buyerOrders.value = result.data
  } catch (error) {
    buyerOrderMessage.value = error instanceof Error ? error.message : '订单加载失败'
  } finally {
    buyerOrderLoading.value = false
  }
}

function resetBuyerOrderFilters() {
  Object.assign(buyerOrderFilters, {
    rechargeAccount: '',
    productName: '',
    productId: '',
    orderNo: '',
    channelOrderNo: '',
    status: '',
    paymentMethod: '',
    startTime: '',
    endTime: '',
  })
  loadBuyerOrders()
}

function setBuyerOrderDateRange(range: 'today' | 'yesterday' | 'week' | 'month' | 'twoMonths' | 'threeMonths') {
  const now = new Date()
  const start = new Date(now)
  const end = new Date(now)
  if (range === 'yesterday') {
    start.setDate(now.getDate() - 1)
    end.setDate(now.getDate() - 1)
  } else if (range === 'week') {
    start.setDate(now.getDate() - 6)
  } else if (range === 'month') {
    start.setMonth(now.getMonth() - 1)
  } else if (range === 'twoMonths') {
    start.setMonth(now.getMonth() - 2)
  } else if (range === 'threeMonths') {
    start.setMonth(now.getMonth() - 3)
  }
  start.setHours(0, 0, 0, 0)
  end.setHours(23, 59, 59, 0)
  buyerOrderFilters.startTime = formatInputDateTime(start)
  buyerOrderFilters.endTime = formatInputDateTime(end)
  loadBuyerOrders()
}

async function loadAdminOrders() {
  adminOrderLoading.value = true
  adminOrderMessage.value = ''
  try {
    const params = new URLSearchParams()
    if (adminOrderFilters.orderNo.trim()) params.set('orderNo', adminOrderFilters.orderNo.trim())
    if (adminOrderFilters.status) params.set('status', adminOrderFilters.status)
    if (adminOrderFilters.productName.trim()) params.set('productName', adminOrderFilters.productName.trim())
    if (adminOrderFilters.rechargeAccount.trim()) params.set('rechargeAccount', adminOrderFilters.rechargeAccount.trim())
    if (adminOrderFilters.paymentMethod) params.set('paymentMethod', adminOrderFilters.paymentMethod)
    const query = params.toString()
    const result = await requestJson<VirtualOrderResponse[]>(`/api/admin/orders${query ? `?${query}` : ''}`)
    adminOrders.value = result.data
  } catch (error) {
    adminOrderMessage.value = error instanceof Error ? error.message : '订单加载失败'
  } finally {
    adminOrderLoading.value = false
  }
}

async function loadAdminUsers() {
  adminUserLoading.value = true
  adminUserMessage.value = ''
  try {
    const params = new URLSearchParams()
    if (adminUserFilters.keyword.trim()) params.set('keyword', adminUserFilters.keyword.trim())
    if (adminUserFilters.status !== '') params.set('status', String(adminUserFilters.status))
    const query = params.toString()
    const result = await requestJson<AdminUserResponse[]>(`/api/admin/users${query ? `?${query}` : ''}`)
    adminUsers.value = result.data
  } catch (error) {
    adminUserMessage.value = error instanceof Error ? error.message : '用户加载失败'
  } finally {
    adminUserLoading.value = false
  }
}

function resetAdminUserFilters() {
  Object.assign(adminUserFilters, { keyword: '', status: '' })
  loadAdminUsers()
}

async function changeAdminUserStatus(user: AdminUserResponse) {
  adminUserMessage.value = ''
  try {
    const status = user.status === 1 ? 0 : 1
    await requestJson<AdminUserResponse>(`/api/admin/users/${user.id}/status`, {
      method: 'PATCH',
      body: { status },
    })
    await loadAdminUsers()
  } catch (error) {
    adminUserMessage.value = error instanceof Error ? error.message : '用户状态更新失败'
  }
}

async function adjustAdminUserBalance(user: AdminUserResponse) {
  const input = window.prompt('请输入余额调整金额，正数为充值，负数为扣减', '0.00')
  if (input == null) return
  const amount = Number(input)
  if (!Number.isFinite(amount) || amount === 0) {
    adminUserMessage.value = '请输入非零数字金额'
    return
  }
  adminUserMessage.value = ''
  try {
    await requestJson<AdminUserResponse>(`/api/admin/users/${user.id}/balance`, {
      method: 'PATCH',
      body: { amount },
    })
    await loadAdminUsers()
  } catch (error) {
    adminUserMessage.value = error instanceof Error ? error.message : '余额调整失败'
  }
}

function resetAdminOrderFilters() {
  Object.assign(adminOrderFilters, { orderNo: '', status: '', productName: '', rechargeAccount: '', paymentMethod: '' })
  loadAdminOrders()
}

async function updateAdminOrderStatus(order: VirtualOrderResponse, status: VirtualOrderStatus) {
  const exceptionMessage = status === 'EXCEPTION' ? window.prompt('请输入异常原因', order.exceptionMessage || '') || '' : null
  try {
    await requestJson<VirtualOrderResponse>(`/api/admin/orders/${order.id}/status`, {
      method: 'PATCH',
      body: { status, exceptionMessage },
    })
    await loadAdminOrders()
  } catch (error) {
    adminOrderMessage.value = error instanceof Error ? error.message : '订单状态更新失败'
  }
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

async function loadBuyerStoreData() {
  buyerStoreLoading.value = true
  buyerStoreMessage.value = ''

  try {
    const [categoryResult, productResult] = await Promise.all([
      requestJson<CategoryTreeResponse[]>('/api/public/categories/tree', { auth: false }),
      requestJson<ProductResponse[]>('/api/public/products', { auth: false }),
    ])
    buyerCategories.value = normalizeTree(categoryResult.data).filter((category) => category.status === 1)
    buyerProducts.value = [...productResult.data]
      .filter((product) => product.status === 1)
      .sort((a, b) => (a.sort || 0) - (b.sort || 0) || a.id - b.id)
    buyerProductPage.value = 1
  } catch (error) {
    buyerStoreMessage.value = error instanceof Error ? error.message : '采购商品加载失败'
  } finally {
    buyerStoreLoading.value = false
  }
}

function resolveBuyerCategoryIds(id: number | '') {
  if (id === '') return []
  const category = buyerCategories.value.find((item) => item.id === id)
  if (category) return [category.id, ...(category.children || []).map((child) => child.id)]
  return [id]
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

async function ensureProductOptions() {
  if (categoryTree.value.length === 0) await loadCategories()
  if (pricingTemplates.value.length === 0) await loadPricingTemplates()
  if (orderTemplates.value.length === 0) await loadOrderTemplates()
  if (supplyChannels.value.length === 0) await loadSupplyChannels()
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
    description: '',
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
    description: category.description || '',
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
          description: categoryForm.description.trim() || null,
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
          description: categoryForm.description.trim() || null,
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
  mediaAssetPickerTarget.value = 'category'
  mediaAssetPickerOpen.value = true
  mediaAssetFilters.scene = 'category'
  loadMediaAssets()
}

function selectMediaAsset(asset: MediaAssetResponse) {
  if (mediaAssetPickerTarget.value === 'product') {
    productForm.image = asset.url
  } else {
    categoryForm.icon = asset.url
  }
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

function formatInputDateTime(value: Date) {
  const pad = (item: number) => String(item).padStart(2, '0')
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())} ${pad(value.getHours())}:${pad(value.getMinutes())}:${pad(value.getSeconds())}`
}

function orderStatusLabel(status: VirtualOrderStatus | '') {
  const labels: Record<VirtualOrderStatus, string> = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    SUCCESS: '充值成功',
    REFUNDED: '已退款',
    EXCEPTION: '异常',
  }
  return status ? labels[status] : '全部状态'
}

function paymentMethodLabel(method?: string | null) {
  return method === 'BALANCE' ? '余额支付' : method || '-'
}

function userStatusLabel(status?: number | null) {
  return status === 1 ? '启用' : '禁用'
}

function orderProductImage(order: VirtualOrderResponse) {
  if (!order.productSnapshot) return ''
  try {
    const snapshot = JSON.parse(order.productSnapshot) as { image?: string | null }
    return snapshot.image || ''
  } catch {
    return ''
  }
}

function formatDuration(seconds?: number | null) {
  if (seconds == null) return '-'
  if (seconds < 60) return `${seconds}秒`
  const minutes = Math.floor(seconds / 60)
  const remainSeconds = seconds % 60
  if (minutes < 60) return `${minutes}分${remainSeconds}秒`
  const hours = Math.floor(minutes / 60)
  return `${hours}时${minutes % 60}分`
}

async function loadOrderTemplates() {
  orderTemplateLoading.value = true
  orderTemplateMessage.value = ''

  try {
    const params = new URLSearchParams()
    if (orderTemplateFilters.name.trim()) params.set('name', orderTemplateFilters.name.trim())
    if (orderTemplateFilters.status !== '') params.set('status', String(orderTemplateFilters.status))
    const query = params.toString()
    const result = await requestJson<OrderTemplateResponse[]>(`/api/admin/order-templates${query ? `?${query}` : ''}`)
    orderTemplates.value = [...result.data].sort((a, b) => (a.sort || 0) - (b.sort || 0) || a.id - b.id)
  } catch (error) {
    orderTemplateMessage.value = error instanceof Error ? error.message : '下单模板加载失败'
  } finally {
    orderTemplateLoading.value = false
  }
}

function resetOrderTemplateFilters() {
  Object.assign(orderTemplateFilters, { name: '', status: '' })
  loadOrderTemplates()
}

function openCreateOrderTemplateModal() {
  orderTemplateModalMode.value = 'create'
  Object.assign(orderTemplateForm, {
    id: undefined,
    name: '',
    description: '',
    sort: 0,
    status: 1,
    fields: [newOrderTemplateField(1)],
  })
  orderTemplateModalOpen.value = true
}

function openEditOrderTemplateModal(template: OrderTemplateResponse) {
  orderTemplateModalMode.value = 'edit'
  Object.assign(orderTemplateForm, {
    id: template.id,
    name: template.name,
    description: template.description || '',
    sort: template.sort,
    status: template.status,
    fields: template.fields.map((field) => ({ ...field })),
  })
  orderTemplateModalOpen.value = true
}

function newOrderTemplateField(sort: number): OrderTemplateFieldResponse {
  return { fieldType: 'PHONE', fieldName: '手机号', placeholder: '请输入手机号', required: true, sort }
}

function addOrderTemplateField() {
  orderTemplateForm.fields.push(newOrderTemplateField(orderTemplateForm.fields.length + 1))
}

function removeOrderTemplateField(index: number) {
  if (orderTemplateForm.fields.length <= 1) return
  orderTemplateForm.fields.splice(index, 1)
  orderTemplateForm.fields.forEach((field, fieldIndex) => {
    field.sort = fieldIndex + 1
  })
}

function applyFieldTypeDefaults(field: OrderTemplateFieldResponse) {
  const defaults: Record<OrderFieldType, { name: string; placeholder: string }> = {
    PHONE: { name: '手机号', placeholder: '请输入手机号' },
    QQ: { name: 'QQ号', placeholder: '请输入QQ号' },
    EMAIL: { name: '邮箱号', placeholder: '请输入邮箱号' },
    ADDRESS: { name: '收货地址', placeholder: '请输入收货地址' },
    TEXT: { name: '自定义信息', placeholder: '请输入信息' },
  }
  field.fieldName = defaults[field.fieldType].name
  field.placeholder = defaults[field.fieldType].placeholder
}

async function saveOrderTemplate() {
  if (!orderTemplateForm.name.trim() || !orderTemplateForm.fields.length || savingOrderTemplate.value) return
  savingOrderTemplate.value = true
  orderTemplateMessage.value = ''
  const body = {
    name: orderTemplateForm.name.trim(),
    description: orderTemplateForm.description.trim() || null,
    sort: Number(orderTemplateForm.sort) || 0,
    status: orderTemplateForm.status,
    fields: orderTemplateForm.fields.map((field, index) => ({
      fieldType: field.fieldType,
      fieldName: field.fieldName.trim(),
      placeholder: field.placeholder?.trim() || null,
      required: field.required,
      sort: Number(field.sort) || index + 1,
    })),
  }

  try {
    if (orderTemplateModalMode.value === 'create') {
      await requestJson<OrderTemplateResponse>('/api/admin/order-templates', { method: 'POST', body })
    } else {
      await requestJson<OrderTemplateResponse>(`/api/admin/order-templates/${orderTemplateForm.id}`, { method: 'PUT', body })
    }
    orderTemplateModalOpen.value = false
    await loadOrderTemplates()
  } catch (error) {
    orderTemplateMessage.value = error instanceof Error ? error.message : '下单模板保存失败'
  } finally {
    savingOrderTemplate.value = false
  }
}

async function changeOrderTemplateStatus(template: OrderTemplateResponse) {
  const nextStatus = template.status === 1 ? 0 : 1
  template.status = nextStatus
  try {
    await requestJson<OrderTemplateResponse>(`/api/admin/order-templates/${template.id}/status`, {
      method: 'PATCH',
      body: { status: nextStatus },
    })
  } catch (error) {
    template.status = nextStatus === 1 ? 0 : 1
    orderTemplateMessage.value = error instanceof Error ? error.message : '下单模板状态更新失败'
  }
}

async function deleteOrderTemplate(template: OrderTemplateResponse) {
  if (!window.confirm(`确定删除下单模板“${template.name}”吗？`)) return
  orderTemplateMessage.value = ''
  try {
    await requestJson<void>(`/api/admin/order-templates/${template.id}`, { method: 'DELETE' })
    await loadOrderTemplates()
  } catch (error) {
    orderTemplateMessage.value = error instanceof Error ? error.message : '下单模板删除失败'
  }
}

function orderTemplateFieldSummary(template: OrderTemplateResponse) {
  return template.fields.map((field) => field.fieldName).join(' + ')
}

function fieldTypeLabel(fieldType: OrderFieldType) {
  const labels: Record<OrderFieldType, string> = {
    PHONE: '手机号',
    QQ: 'QQ号',
    EMAIL: '邮箱号',
    ADDRESS: '收货地址',
    TEXT: '自定义文本',
  }
  return labels[fieldType]
}

async function loadProducts() {
  productLoading.value = true
  productMessage.value = ''

  try {
    await ensureProductOptions()
    const params = new URLSearchParams()
    if (productFilters.name.trim()) params.set('name', productFilters.name.trim())
    if (productFilters.productType) params.set('productType', productFilters.productType)
    if (productFilters.categoryId !== '') params.set('categoryId', String(productFilters.categoryId))
    if (productFilters.status !== '') params.set('status', String(productFilters.status))
    const query = params.toString()
    const result = await requestJson<ProductResponse[]>(`/api/admin/products${query ? `?${query}` : ''}`)
    products.value = [...result.data].sort((a, b) => (a.sort || 0) - (b.sort || 0) || a.id - b.id)
  } catch (error) {
    productMessage.value = error instanceof Error ? error.message : '商品加载失败'
  } finally {
    productLoading.value = false
  }
}

function resetProductFilters() {
  Object.assign(productFilters, { name: '', productType: '', categoryId: '', status: '' })
  loadProducts()
}

async function openCreateProductModal() {
  await ensureProductOptions()
  productModalMode.value = 'create'
  Object.assign(productForm, {
    id: undefined,
    productType: 'VIRTUAL',
    categoryId: flatCategoryOptions.value[0]?.id || '',
    name: '',
    costPrice: 0,
    terminalLimitPrice: null,
    supplyCostStrategy: 'LOWEST',
    pricingTemplateId: pricingTemplates.value[0]?.id || '',
    image: '',
    faceValue: null,
    orderTemplateId: orderTemplates.value[0]?.id || '',
    minPurchaseQuantity: 1,
    maxPurchaseQuantity: null,
    sort: 0,
    status: 1,
    supplyBindings: [],
  })
  productModalTab.value = 'basic'
  productModalOpen.value = true
}

async function openEditProductModal(product: ProductResponse) {
  await ensureProductOptions()
  productModalMode.value = 'edit'
  Object.assign(productForm, {
    id: product.id,
    productType: product.productType,
    categoryId: product.categoryId,
    name: product.name,
    costPrice: Number(product.costPrice),
    terminalLimitPrice: product.terminalLimitPrice == null ? null : Number(product.terminalLimitPrice),
    supplyCostStrategy: product.supplyCostStrategy || 'LOWEST',
    pricingTemplateId: product.pricingTemplateId,
    image: product.image || '',
    faceValue: product.faceValue == null ? null : Number(product.faceValue),
    orderTemplateId: product.orderTemplateId,
    minPurchaseQuantity: product.minPurchaseQuantity || 1,
    maxPurchaseQuantity: product.maxPurchaseQuantity == null ? null : Number(product.maxPurchaseQuantity),
    sort: product.sort,
    status: product.status,
    supplyBindings: (product.supplyBindings || []).map((binding) => ({
      id: binding.id,
      productId: binding.productId,
      channelId: binding.channelId,
      channelName: binding.channelName,
      channelType: binding.channelType,
      channelProductId: binding.channelProductId,
      channelProductName: binding.channelProductName,
      channelCostPrice: Number(binding.channelCostPrice),
      active: Boolean(binding.active),
      sort: binding.sort,
      status: binding.status,
    })),
  })
  productModalTab.value = 'basic'
  productModalOpen.value = true
}

async function saveProduct() {
  if (!productForm.name.trim() || productForm.categoryId === '' || productForm.pricingTemplateId === '' || productForm.orderTemplateId === '' || savingProduct.value) return
  const invalidBinding = productForm.supplyBindings.find(
    (binding) => binding.channelId !== '' && !binding.channelProductId.trim(),
  )
  if (invalidBinding) {
    productMessage.value = '已选择货源渠道时，商品编号必填'
    productModalTab.value = 'supply'
    return
  }
  savingProduct.value = true
  productMessage.value = ''
  const body = {
    productType: productForm.productType,
    categoryId: Number(productForm.categoryId),
    name: productForm.name.trim(),
    costPrice: resolveProductCostPrice(),
    terminalLimitPrice: productForm.terminalLimitPrice == null ? null : Number(productForm.terminalLimitPrice),
    supplyCostStrategy: productForm.supplyCostStrategy,
    pricingTemplateId: Number(productForm.pricingTemplateId),
    image: productForm.image.trim() || null,
    faceValue: productForm.faceValue == null ? null : Number(productForm.faceValue),
    orderTemplateId: Number(productForm.orderTemplateId),
    minPurchaseQuantity: Number(productForm.minPurchaseQuantity) || 1,
    maxPurchaseQuantity: productForm.maxPurchaseQuantity == null ? null : Number(productForm.maxPurchaseQuantity),
    sort: Number(productForm.sort) || 0,
    status: productForm.status,
    supplyBindings: productForm.supplyBindings
      .filter((binding) => binding.channelId !== '' && binding.channelProductId.trim() && binding.channelProductName.trim())
      .map((binding, index) => ({
        channelId: Number(binding.channelId),
        channelProductId: binding.channelProductId.trim(),
        channelProductName: binding.channelProductName.trim(),
        channelCostPrice: Number(binding.channelCostPrice),
        active: Boolean(binding.active),
        sort: Number(binding.sort) || index + 1,
        status: binding.status,
      })),
  }

  try {
    if (productModalMode.value === 'create') {
      await requestJson<ProductResponse>('/api/admin/products', { method: 'POST', body })
    } else {
      await requestJson<ProductResponse>(`/api/admin/products/${productForm.id}`, { method: 'PUT', body })
    }
    productModalOpen.value = false
    await loadProducts()
  } catch (error) {
    productMessage.value = error instanceof Error ? error.message : '商品保存失败'
  } finally {
    savingProduct.value = false
  }
}

async function changeProductStatus(product: ProductResponse) {
  const nextStatus = product.status === 1 ? 0 : 1
  product.status = nextStatus
  try {
    await requestJson<ProductResponse>(`/api/admin/products/${product.id}/status`, {
      method: 'PATCH',
      body: { status: nextStatus },
    })
  } catch (error) {
    product.status = nextStatus === 1 ? 0 : 1
    productMessage.value = error instanceof Error ? error.message : '商品状态更新失败'
  }
}

async function deleteProduct(product: ProductResponse) {
  if (!window.confirm(`确定删除商品“${product.name}”吗？`)) return
  productMessage.value = ''
  try {
    await requestJson<void>(`/api/admin/products/${product.id}`, { method: 'DELETE' })
    await loadProducts()
  } catch (error) {
    productMessage.value = error instanceof Error ? error.message : '商品删除失败'
  }
}

async function uploadProductImage(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || productImageUploading.value) return
  productImageUploading.value = true
  productMessage.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('scene', 'product')
    const result = await requestForm<ImageUploadResponse>('/api/admin/files/images', formData)
    productForm.image = result.data.url
    await loadMediaAssets()
  } catch (error) {
    productMessage.value = error instanceof Error ? error.message : '商品图片上传失败'
  } finally {
    productImageUploading.value = false
    input.value = ''
  }
}

function openProductMediaAssetPicker() {
  mediaAssetPickerTarget.value = 'product'
  mediaAssetPickerOpen.value = true
  mediaAssetFilters.scene = 'product'
  loadMediaAssets()
}

function productTypeLabel(productType: ProductType) {
  const labels: Record<ProductType, string> = {
    VIRTUAL: '虚拟商品',
    CARD: '卡密商品',
    NORMAL: '普通商品',
  }
  return labels[productType]
}

function formatMoney(value?: number | string | null) {
  if (value == null) return '-'
  const amount = Number(value)
  return Number.isFinite(amount) ? `¥${amount.toFixed(2)}` : String(value)
}

async function loadSupplyChannels() {
  supplyChannelLoading.value = true
  supplyChannelMessage.value = ''

  try {
    const params = new URLSearchParams()
    if (supplyChannelFilters.name.trim()) params.set('name', supplyChannelFilters.name.trim())
    if (supplyChannelFilters.channelType) params.set('channelType', supplyChannelFilters.channelType)
    if (supplyChannelFilters.status !== '') params.set('status', String(supplyChannelFilters.status))
    const query = params.toString()
    const result = await requestJson<SupplyChannelResponse[]>(`/api/admin/supply-channels${query ? `?${query}` : ''}`)
    supplyChannels.value = [...result.data].sort((a, b) => (a.sort || 0) - (b.sort || 0) || a.id - b.id)
  } catch (error) {
    supplyChannelMessage.value = error instanceof Error ? error.message : '货源渠道加载失败'
  } finally {
    supplyChannelLoading.value = false
  }
}

function resetSupplyChannelFilters() {
  Object.assign(supplyChannelFilters, { name: '', channelType: '', status: '' })
  loadSupplyChannels()
}

function openCreateSupplyChannelModal() {
  supplyChannelModalMode.value = 'create'
  Object.assign(supplyChannelForm, {
    id: undefined,
    channelType: 'YOUKAYUN',
    name: '',
    apiUrl: '',
    userId: '',
    secretKey: '',
    sort: 0,
    status: 1,
  })
  supplyChannelModalOpen.value = true
}

function openEditSupplyChannelModal(channel: SupplyChannelResponse) {
  supplyChannelModalMode.value = 'edit'
  Object.assign(supplyChannelForm, {
    id: channel.id,
    channelType: channel.channelType,
    name: channel.name,
    apiUrl: channel.apiUrl,
    userId: channel.userId,
    secretKey: '',
    sort: channel.sort,
    status: channel.status,
  })
  supplyChannelModalOpen.value = true
}

async function saveSupplyChannel() {
  if (!supplyChannelForm.name.trim() || !supplyChannelForm.apiUrl.trim() || !supplyChannelForm.userId.trim() || savingSupplyChannel.value) return
  if (supplyChannelModalMode.value === 'create' && !supplyChannelForm.secretKey.trim()) return
  savingSupplyChannel.value = true
  supplyChannelMessage.value = ''
  const body = {
    channelType: supplyChannelForm.channelType,
    name: supplyChannelForm.name.trim(),
    apiUrl: supplyChannelForm.apiUrl.trim(),
    userId: supplyChannelForm.userId.trim(),
    secretKey: supplyChannelForm.secretKey.trim() || null,
    sort: Number(supplyChannelForm.sort) || 0,
    status: supplyChannelForm.status,
  }

  try {
    if (supplyChannelModalMode.value === 'create') {
      await requestJson<SupplyChannelResponse>('/api/admin/supply-channels', { method: 'POST', body })
    } else {
      await requestJson<SupplyChannelResponse>(`/api/admin/supply-channels/${supplyChannelForm.id}`, { method: 'PUT', body })
    }
    supplyChannelModalOpen.value = false
    await loadSupplyChannels()
  } catch (error) {
    supplyChannelMessage.value = error instanceof Error ? error.message : '货源渠道保存失败'
  } finally {
    savingSupplyChannel.value = false
  }
}

async function changeSupplyChannelStatus(channel: SupplyChannelResponse) {
  const nextStatus = channel.status === 1 ? 0 : 1
  channel.status = nextStatus
  try {
    await requestJson<SupplyChannelResponse>(`/api/admin/supply-channels/${channel.id}/status`, {
      method: 'PATCH',
      body: { status: nextStatus },
    })
  } catch (error) {
    channel.status = nextStatus === 1 ? 0 : 1
    supplyChannelMessage.value = error instanceof Error ? error.message : '货源渠道状态更新失败'
  }
}

async function deleteSupplyChannel(channel: SupplyChannelResponse) {
  if (!window.confirm(`确定删除货源渠道“${channel.name}”吗？`)) return
  supplyChannelMessage.value = ''
  try {
    await requestJson<void>(`/api/admin/supply-channels/${channel.id}`, { method: 'DELETE' })
    await loadSupplyChannels()
  } catch (error) {
    supplyChannelMessage.value = error instanceof Error ? error.message : '货源渠道删除失败'
  }
}

async function querySupplyChannelBalance(channel: SupplyChannelResponse) {
  if (queryingSupplyChannelId.value === channel.id) return
  queryingSupplyChannelId.value = channel.id
  supplyChannelMessage.value = ''

  try {
    const result = await requestJson<SupplyChannelBalanceResponse>(`/api/admin/supply-channels/${channel.id}/balance`)
    supplyChannelBalances[channel.id] = result.data.balance == null ? '未返回余额' : formatMoney(result.data.balance)
  } catch (error) {
    supplyChannelBalances[channel.id] = '查询失败'
    supplyChannelMessage.value = error instanceof Error ? error.message : '渠道余额查询失败'
  } finally {
    queryingSupplyChannelId.value = null
  }
}

function supplyChannelTypeLabel(channelType: string) {
  return channelType === 'YOUKAYUN' ? '优卡云' : channelType
}

function addProductSupplyBinding() {
  productForm.supplyBindings.push({
    channelId: supplyChannels.value[0]?.id || '',
    channelProductId: '',
    channelProductName: '',
    channelCostPrice: 0,
    active: productForm.supplyBindings.length === 0,
    sort: productForm.supplyBindings.length + 1,
    status: 1,
  })
}

function removeProductSupplyBinding(index: number) {
  productForm.supplyBindings.splice(index, 1)
  productForm.supplyBindings.forEach((binding, bindingIndex) => {
    binding.sort = bindingIndex + 1
  })
}

function setActiveSupplyBinding(index: number, checked: boolean) {
  productForm.supplyBindings.forEach((binding, bindingIndex) => {
    binding.active = checked && bindingIndex === index
  })
}

function resolveProductCostPrice() {
  const enabledCosts = productForm.supplyBindings
    .filter((binding) => binding.status === 1 && binding.channelId !== '')
    .map((binding) => Number(binding.channelCostPrice))
    .filter((cost) => Number.isFinite(cost) && cost >= 0)
  if (!enabledCosts.length) return Number(productForm.costPrice)
  return productForm.supplyCostStrategy === 'HIGHEST' ? Math.max(...enabledCosts) : Math.min(...enabledCosts)
}

async function syncProductSupplyBinding(binding: ProductSupplyBindingResponse, index: number) {
  if (binding.channelId === '' || !binding.channelProductId.trim() || syncingSupplyBindingIndex.value === index) {
    productMessage.value = '请选择渠道并填写商品编号后再同步'
    productModalTab.value = 'supply'
    return
  }
  syncingSupplyBindingIndex.value = index
  productMessage.value = ''

  try {
    const result = await requestJson<SupplyChannelProductResponse>(
      `/api/admin/supply-channels/${binding.channelId}/products/${encodeURIComponent(binding.channelProductId.trim())}`,
    )
    binding.channelProductName = result.data.channelProductName || binding.channelProductName
    if (result.data.channelCostPrice != null) {
      binding.channelCostPrice = Number(result.data.channelCostPrice)
    }
    productMessage.value = '货源渠道商品同步成功'
  } catch (error) {
    productMessage.value = error instanceof Error ? error.message : '货源渠道商品同步失败'
  } finally {
    syncingSupplyBindingIndex.value = null
  }
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

function readUserToken() {
  return localStorage.getItem('zerolanshop_token') || sessionStorage.getItem('zerolanshop_token')
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

async function requestUserJson<T>(
  endpoint: string,
  options: { method?: string; body?: unknown } = {},
) {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  const token = readUserToken()
  if (token) headers.Authorization = `Bearer ${token}`

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
        <button :class="{ active: buyerView === 'dashboard' }" type="button" @click="navigate('/index')"><Home :size="20" />主页</button>
        <button :class="{ active: buyerView === 'benefits' || buyerView === 'products' }" type="button" @click="navigate('/purchase/benefits')"><ShoppingCart :size="20" />采购中心</button>
        <button :class="{ active: buyerView === 'orders' }" type="button" @click="navigate('/purchase/orders')"><Box :size="20" />订单管理</button>
        <button type="button"><WalletCards :size="20" />财务管理</button>
        <button type="button"><CreditCard :size="20" />卡密中心</button>
        <button type="button"><BarChart3 :size="20" />报表中心</button>
        <button type="button"><Package :size="20" />合同管理</button>
        <button type="button"><UserRound :size="20" />个人中心</button>
      </nav>
    </aside>

    <section class="buyer-main">
      <header class="buyer-topbar">
        <h1 v-if="buyerView === 'dashboard'">采购工作台</h1>
        <h1 v-else-if="buyerView === 'orders'">订单管理</h1>
        <nav v-else-if="buyerView === 'benefits' || buyerView === 'products'" class="buyer-section-tabs" aria-label="采购中心栏目">
          <button :class="{ active: buyerView === 'benefits' }" type="button" @click="switchBuyerView('benefits')">权益中心</button>
          <button :class="{ active: buyerView === 'products' }" type="button" @click="switchBuyerView('products')">全部商品</button>
        </nav>
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
        <p v-if="buyerStoreMessage" class="buyer-message">{{ buyerStoreMessage }}</p>

        <section v-if="buyerView === 'benefits'" class="buyer-store-view">
          <div class="buyer-search-row">
            <label class="buyer-search">
              <Search :size="19" />
              <input :value="buyerSearch" placeholder="搜索商品分类" type="search" @input="updateBuyerSearch(($event.target as HTMLInputElement).value)" />
            </label>
          </div>

          <nav class="buyer-category-tabs" aria-label="权益分类">
            <button
              v-for="category in buyerCategoryTabs"
              :key="category.id === '' ? 'all' : category.id"
              :class="{ active: buyerCategoryId === category.id }"
              type="button"
              @click="selectBuyerCategory(category.id)"
            >
              {{ category.name }}
            </button>
          </nav>

          <div class="benefit-grid">
            <article v-for="category in visibleBuyerCategories" :key="category.id" class="benefit-card">
              <span class="benefit-thumb">
                <img v-if="isImageIcon(category.icon)" :src="category.icon || ''" :alt="category.name" />
                <span v-else-if="category.icon">{{ category.icon }}</span>
                <Package v-else :size="24" />
              </span>
              <div>
                <h2>{{ category.name }}</h2>
                <p>{{ category.description || '-' }}</p>
                <small>共 {{ buyerCategoryProductCount(category) }} 个商品</small>
              </div>
              <button class="buy-action" type="button" @click="openBuyerCategoryProducts(category)">查看</button>
            </article>
            <div v-if="!visibleBuyerCategories.length && !buyerStoreLoading" class="empty-row">暂无商品分类</div>
          </div>
          <p v-if="buyerStoreLoading" class="toolbar-note">正在加载商品分类...</p>
        </section>

        <section v-else-if="buyerView === 'products'" class="buyer-store-view">
          <div class="buyer-product-toolbar">
            <label class="buyer-search wide">
              <Search :size="19" />
              <input :value="buyerSearch" placeholder="请输入商品搜索" type="search" @input="updateBuyerSearch(($event.target as HTMLInputElement).value)" @keyup.enter="buyerProductPage = 1" />
            </label>
            <div class="buyer-category-picker">
              <button class="buyer-category-trigger" type="button" @click="toggleBuyerCategoryPicker">
                <span>{{ buyerCategoryPickerLabel }}</span>
                <ChevronDown :size="16" />
              </button>
              <div v-if="buyerCategoryPickerOpen" class="buyer-category-panel">
                <div class="buyer-category-column parent">
                  <button
                    v-for="category in buyerCategories"
                    :key="category.id"
                    :class="{ active: activeBuyerParentCategory?.id === category.id }"
                    type="button"
                    @click="selectBuyerParentCategory(category)"
                  >
                    <span class="radio-dot" :class="{ checked: buyerCategoryId === category.id }"></span>
                    <span>{{ category.name }}</span>
                    <ChevronRight v-if="category.children?.length" :size="15" />
                  </button>
                </div>
                <div class="buyer-category-column child">
                  <button
                    v-for="child in activeBuyerParentCategory?.children || []"
                    :key="child.id"
                    :class="{ active: buyerCategoryId === child.id }"
                    type="button"
                    @click="selectBuyerChildCategory(child)"
                  >
                    <span class="radio-dot" :class="{ checked: buyerCategoryId === child.id }"></span>
                    <span>{{ child.name }}</span>
                  </button>
                  <div v-if="activeBuyerParentCategory && !activeBuyerParentCategory.children?.length" class="category-empty">暂无子分类</div>
                </div>
              </div>
            </div>
            <select :value="buyerProductStatus" @change="updateBuyerProductStatusFromValue(($event.target as HTMLSelectElement).value)">
              <option value="">全部状态</option>
              <option :value="1">正在销售</option>
              <option :value="0">已下架</option>
            </select>
            <button class="primary-action" type="button" @click="buyerProductPage = 1">搜索</button>
            <button class="soft-action" type="button" @click="resetBuyerProductFilters">重置</button>
          </div>

          <div class="buyer-product-table">
            <div class="buyer-product-row table-head" role="row">
              <span>商品信息</span>
              <span>面额</span>
              <span>采购价</span>
              <span>终端限价</span>
              <span>商品规则</span>
              <span>商品状态</span>
              <span>配置时间</span>
              <span>操作</span>
            </div>
            <div v-for="product in pagedBuyerProducts" :key="product.id" class="buyer-product-row" role="row">
              <span class="buyer-product-info">
                <span class="benefit-thumb small">
                  <img v-if="product.image" :src="product.image" :alt="product.name" />
                  <Package v-else :size="18" />
                </span>
                <span>
                  <strong>{{ product.name }}</strong>
                  <small>分类：{{ product.categoryName || '-' }}</small>
                </span>
              </span>
              <span>{{ product.faceValue ? formatMoney(product.faceValue) : '-' }}</span>
              <strong class="cost-price">{{ formatMoney(product.salePrice) }}</strong>
              <span>{{ formatMoney(product.terminalLimitPrice) }}</span>
              <span class="product-rule-cell">
                <small>账号规则：{{ productRuleTemplateLabel(product) }}</small>
                <small>充值数量：{{ formatPurchaseQuantityRange(product) }}</small>
              </span>
              <span class="sale-status"><i></i>{{ product.status === 1 ? '正在销售' : '已下架' }}</span>
              <span>{{ formatDateTime(product.updateTime || product.createTime) }}</span>
              <button class="text-action edit" type="button" @click="openPurchaseModal(product)">采购</button>
            </div>
            <div v-if="!pagedBuyerProducts.length && !buyerStoreLoading" class="empty-row">暂无商品数据</div>
          </div>

          <div class="buyer-pagination">
            <span>共 {{ filteredBuyerProducts.length }} 条</span>
            <div>
              <button class="soft-action pager-button" type="button" :disabled="buyerProductPage <= 1" @click="buyerProductPage--">&lt;</button>
              <button
                v-for="pageNumber in buyerProductsPageCount"
                :key="pageNumber"
                class="soft-action pager-button"
                :class="{ active: buyerProductPage === pageNumber }"
                type="button"
                v-show="pageNumber <= 3 || pageNumber === buyerProductsPageCount || Math.abs(pageNumber - buyerProductPage) <= 1"
                @click="buyerProductPage = pageNumber"
              >
                {{ pageNumber }}
              </button>
              <button class="soft-action pager-button" type="button" :disabled="buyerProductPage >= buyerProductsPageCount" @click="buyerProductPage++">&gt;</button>
              <select :value="buyerProductPageSize" @change="updateBuyerPageSize(Number(($event.target as HTMLSelectElement).value))">
                <option :value="10">10 条/页</option>
                <option :value="20">20 条/页</option>
                <option :value="50">50 条/页</option>
              </select>
            </div>
          </div>
          <p v-if="buyerStoreLoading" class="toolbar-note">正在加载商品...</p>
        </section>

        <section v-else-if="buyerView === 'orders'" class="buyer-store-view buyer-order-view">
          <div class="buyer-order-toolbar">
            <input v-model="buyerOrderFilters.rechargeAccount" placeholder="请输入充值账号" type="text" @keyup.enter="loadBuyerOrders" />
            <input v-model="buyerOrderFilters.productName" placeholder="请输入商品名称" type="text" @keyup.enter="loadBuyerOrders" />
            <input v-model="buyerOrderFilters.productId" placeholder="请输入商品编号" type="text" @keyup.enter="loadBuyerOrders" />
            <input v-model="buyerOrderFilters.orderNo" placeholder="请输入订单号" type="text" @keyup.enter="loadBuyerOrders" />
            <input v-model="buyerOrderFilters.channelOrderNo" placeholder="请输入外部订单号" type="text" @keyup.enter="loadBuyerOrders" />
            <select v-model="buyerOrderFilters.status">
              <option value="">请选择充值状态</option>
              <option value="PENDING">待处理</option>
              <option value="PROCESSING">处理中</option>
              <option value="SUCCESS">充值成功</option>
              <option value="REFUNDED">已退款</option>
              <option value="EXCEPTION">异常</option>
            </select>
            <input v-model="buyerOrderFilters.startTime" placeholder="开始时间" type="text" />
            <input v-model="buyerOrderFilters.endTime" placeholder="结束时间" type="text" />
            <button class="primary-action" type="button" @click="loadBuyerOrders">查询</button>
            <button class="soft-action" type="button" @click="resetBuyerOrderFilters">重置</button>
          </div>
          <div class="order-shortcuts">
            <button class="soft-action" type="button" @click="setBuyerOrderDateRange('today')">今天</button>
            <button class="soft-action" type="button" @click="setBuyerOrderDateRange('yesterday')">昨天</button>
            <button class="soft-action" type="button" @click="setBuyerOrderDateRange('week')">近一周</button>
            <button class="soft-action" type="button" @click="setBuyerOrderDateRange('month')">近一月</button>
            <button class="soft-action" type="button" @click="setBuyerOrderDateRange('twoMonths')">近俩月</button>
            <button class="soft-action" type="button" @click="setBuyerOrderDateRange('threeMonths')">近仨月</button>
          </div>
          <p v-if="buyerOrderMessage" class="buyer-message">{{ buyerOrderMessage }}</p>
          <div class="buyer-order-table">
            <div class="buyer-order-row table-head" role="row">
              <span>商品信息</span>
              <span>订单号</span>
              <span>数量</span>
              <span>金额</span>
              <span>类型</span>
              <span>充值账号</span>
              <span>创建时间</span>
              <span>处理状态</span>
              <span>操作</span>
            </div>
            <div v-for="order in buyerOrders" :key="order.id" class="buyer-order-row" role="row">
              <span class="buyer-product-info">
                <span class="benefit-thumb small">
                  <img v-if="orderProductImage(order)" :src="orderProductImage(order)" :alt="order.productName" />
                  <Package v-else :size="18" />
                </span>
                <span>
                  <strong>{{ order.productName }}</strong>
                  <small>编号：{{ order.productId }}</small>
                </span>
              </span>
              <span>{{ order.orderNo }}</span>
              <span>{{ order.quantity }}</span>
              <strong>{{ formatMoney(order.orderAmount) }}</strong>
              <span class="direct-tag">直充</span>
              <span>{{ order.rechargeAccount }}</span>
              <span>{{ formatDateTime(order.createdAt) }}</span>
              <span class="order-status" :class="order.status.toLowerCase()">{{ orderStatusLabel(order.status) }}</span>
              <span class="row-actions">
                <button class="text-action edit" type="button" :title="order.exceptionMessage || order.channelOrderNo || '订单详情'"><FileText :size="14" /></button>
              </span>
            </div>
            <div v-if="!buyerOrders.length && !buyerOrderLoading" class="empty-row">暂无订单数据</div>
          </div>
          <div class="buyer-pagination">
            <span>共 {{ buyerOrders.length }} 条</span>
            <div>
              <button class="soft-action pager-button" type="button" disabled>&lt;</button>
              <button class="soft-action pager-button active" type="button">1</button>
              <button class="soft-action pager-button" type="button" disabled>&gt;</button>
              <select>
                <option>10 条/页</option>
              </select>
            </div>
          </div>
          <p v-if="buyerOrderLoading" class="toolbar-note">正在加载订单...</p>
        </section>

        <section v-else class="quick-section">
          <h2>常用功能</h2>
          <div class="quick-grid">
            <button class="quick-card" type="button" aria-label="权益采购" @click="navigate('/purchase/benefits')">
              <span class="quick-icon"><HandCoins :size="40" :stroke-width="2.1" /></span>
              <span>权益采购</span>
            </button>
            <button class="quick-card" type="button" aria-label="订单记录" @click="navigate('/purchase/orders')">
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

        <section v-if="buyerView === 'dashboard'" class="buyer-grid">
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

      <div v-if="purchaseModalOpen && purchasingProduct" class="modal-mask purchase-modal-mask" role="dialog" aria-modal="true">
        <section class="purchase-modal">
          <header>
            <h2>直充在线购买</h2>
            <button type="button" aria-label="关闭" @click="purchaseModalOpen = false"><X :size="18" /></button>
          </header>
          <div class="purchase-product-line">
            <span class="benefit-thumb">
              <img v-if="purchasingProduct.image" :src="purchasingProduct.image" :alt="purchasingProduct.name" />
              <Package v-else :size="24" />
            </span>
            <div>
              <strong>{{ purchasingProduct.name }}</strong>
              <small>{{ purchasingProduct.categoryName || '-' }}</small>
            </div>
            <span class="direct-tag">直充</span>
            <small>编号{{ purchasingProduct.id }}</small>
          </div>
          <dl class="purchase-summary">
            <div><dt>单价</dt><dd>{{ formatMoney(purchasingProduct.salePrice) }}</dd></div>
          </dl>
          <label class="purchase-field">
            <span><b>*</b> 充值账号</span>
            <textarea v-model="purchaseRechargeAccount" placeholder="每行填写一个充值账号"></textarea>
            <small>已识别 {{ purchaseAccounts.length }} 个账号，每行会创建一个订单</small>
          </label>
          <div class="form-grid two">
            <label class="purchase-field">
              <span>充值数量</span>
              <input
                v-model.number="purchaseQuantity"
                :min="purchasingProduct.minPurchaseQuantity || 1"
                :max="purchasingProduct.maxPurchaseQuantity || undefined"
                :readonly="isFixedPurchaseQuantity(purchasingProduct)"
                type="number"
                @blur="normalizePurchaseQuantity"
                @change="normalizePurchaseQuantity"
              />
              <small>允许数量：{{ formatPurchaseQuantityRange(purchasingProduct) }}</small>
            </label>
            <label class="purchase-field">
              <span>支付方式</span>
              <select v-model="purchasePaymentMethod">
                <option value="BALANCE">余额支付</option>
              </select>
              <small>&nbsp;</small>
            </label>
          </div>
          <p v-if="purchaseMessage" class="category-message">{{ purchaseMessage }}</p>
          <footer>
            <div class="purchase-total">
              <small>共 {{ purchaseAccounts.length }} 个账号，每个账号 {{ purchaseQuantity || 0 }} 件，合计支付：</small>
              <strong>{{ formatMoney(purchaseTotalAmount) }}</strong>
              <small class="purchase-balance">当前余额：{{ formatMoney(currentBalance) }}</small>
            </div>
            <label class="inline-check">
              <input v-model="purchaseConfirmed" type="checkbox" />
              <span>我已确认账号、购买数量、订单金额无误。</span>
            </label>
            <div class="purchase-actions">
              <button class="soft-action" type="button" @click="purchaseModalOpen = false">取消</button>
              <button class="primary-action danger" type="button" :disabled="!canSubmitPurchase" @click="submitPurchase">
                {{ purchaseSubmitting ? '支付中...' : '确认支付' }}
              </button>
            </div>
          </footer>
        </section>
      </div>

      <div v-if="buyerCategoryModalOpen" class="modal-mask buyer-category-modal-mask" role="dialog" aria-modal="true">
        <section
          class="buyer-category-products-modal"
          :class="{ dragging: buyerCategoryModalDragging }"
          :style="{ transform: `translate(${buyerCategoryModalPosition.x}px, ${buyerCategoryModalPosition.y}px)` }"
        >
          <header class="draggable-modal-header" @pointerdown="startBuyerCategoryModalDrag">
            <div>
              <h2>{{ viewingBuyerCategory?.name }}</h2>
              <p>当前分类下共 {{ viewingCategoryProducts.length }} 个商品</p>
            </div>
            <button type="button" aria-label="关闭" @pointerdown.stop @click="buyerCategoryModalOpen = false"><X :size="18" /></button>
          </header>

          <div class="buyer-modal-products">
            <article v-for="product in viewingCategoryProducts" :key="product.id" class="buyer-modal-product">
              <span class="benefit-thumb modal-product-thumb">
                <img v-if="product.image" :src="product.image" :alt="product.name" />
                <Package v-else :size="24" />
              </span>
              <div class="buyer-modal-product-main">
                <strong>{{ product.name }}</strong>
                <small>编号 {{ product.id }} · 分类 {{ product.categoryName || '-' }}</small>
                <div class="buyer-modal-product-tags">
                  <span>单价 {{ formatMoney(product.salePrice) }}</span>
                  <span>{{ product.faceValue ? `面值 ${formatMoney(product.faceValue)}` : '面值 -' }}</span>
                  <span>数量 {{ formatPurchaseQuantityRange(product) }}</span>
                  <span>{{ product.orderTemplateName || '默认下单模板' }}</span>
                </div>
              </div>
              <div class="buyer-modal-product-side">
                <span class="sale-status"><i></i>{{ product.status === 1 ? '正在销售' : '已下架' }}</span>
                <small>{{ formatDateTime(product.updateTime || product.createTime) }}</small>
              </div>
              <button class="buy-action inline" type="button" @click="openPurchaseModal(product)">采购</button>
            </article>
            <div v-if="!viewingCategoryProducts.length" class="empty-row">当前分类暂无商品</div>
          </div>
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
            :class="{ active: isAdminMenuActive(menu), expanded: menu.children?.length && menu.expanded }"
            @click="handleAdminMenuClick(menu)"
          >
            <component :is="menu.icon" :size="18" />
            <span>{{ menu.name }}</span>
            <ChevronDown v-if="menu.children?.length && menu.expanded" :size="16" class="nav-arrow" />
            <ChevronRight v-else-if="menu.children?.length" :size="16" class="nav-arrow" />
          </button>
          <div v-if="menu.children && menu.expanded" class="sub-nav">
            <button
              v-for="child in menu.children"
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

      <div v-else-if="adminView === 'product'" class="admin-content">
        <section class="page-heading">
          <h2>商品管理</h2>
          <p>维护商品基础信息、售价、图片、下单模板和购买限制</p>
        </section>

        <section class="category-panel">
          <div class="category-toolbar pricing-toolbar">
            <button class="primary-action" type="button" @click="openCreateProductModal()">
              <Plus :size="18" />
              <span>添加商品</span>
            </button>
            <label>
              <span>商品名称</span>
              <input v-model="productFilters.name" placeholder="输入名称筛选" type="text" @keyup.enter="loadProducts" />
            </label>
            <label>
              <span>商品类型</span>
              <select v-model="productFilters.productType">
                <option value="">全部</option>
                <option value="VIRTUAL">虚拟商品</option>
                <option value="CARD">卡密商品</option>
                <option value="NORMAL">普通商品</option>
              </select>
            </label>
            <label>
              <span>商品分类</span>
              <select v-model="productFilters.categoryId">
                <option value="">全部</option>
                <option v-for="item in flatCategoryOptions" :key="item.id" :value="item.id">{{ item.name }}</option>
              </select>
            </label>
            <label>
              <span>状态</span>
              <select v-model="productFilters.status">
                <option value="">全部</option>
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </label>
            <button class="soft-action" type="button" @click="loadProducts">筛选</button>
            <button class="soft-action" type="button" @click="resetProductFilters">重置</button>
            <p v-if="productLoading" class="toolbar-note">正在加载商品...</p>
          </div>

          <p v-if="productMessage" class="category-message">{{ productMessage }}</p>

          <div class="category-table" role="table" aria-label="商品列表">
            <div class="product-row table-head" role="row">
              <span>图片</span>
              <span>商品</span>
              <span>分类</span>
              <span>成本价</span>
              <span>售价</span>
              <span>终端限价</span>
              <span>面值</span>
              <span>定价模板</span>
              <span>下单模板</span>
              <span>限购</span>
              <span>状态</span>
              <span>操作</span>
            </div>

            <div v-for="product in products" :key="product.id" class="product-row" role="row">
              <span class="product-thumb">
                <img v-if="product.image" :src="product.image" alt="" />
                <Package v-else :size="20" />
              </span>
              <span class="template-name">
                <strong>{{ product.name }}</strong>
                <small>{{ productTypeLabel(product.productType) }} · #{{ product.id }}</small>
              </span>
              <span>{{ product.categoryName || '-' }}</span>
              <strong>{{ formatMoney(product.costPrice) }}</strong>
              <strong class="pricing-value">{{ formatMoney(product.salePrice) }}</strong>
              <span>{{ formatMoney(product.terminalLimitPrice) }}</span>
              <span>{{ formatMoney(product.faceValue) }}</span>
              <span>{{ product.pricingTemplateName || '-' }}</span>
              <span>{{ product.orderTemplateName || '-' }}</span>
              <span>{{ product.minPurchaseQuantity }} - {{ product.maxPurchaseQuantity || '不限' }}</span>
              <button class="switch" :class="{ on: product.status === 1 }" type="button" @click="changeProductStatus(product)">
                <span></span>
              </button>
              <span class="row-actions">
                <button class="text-action edit" type="button" @click="openEditProductModal(product)">
                  <Edit3 :size="14" />
                  编辑
                </button>
                <button class="text-action danger" type="button" @click="deleteProduct(product)">
                  <Trash2 :size="14" />
                  删除
                </button>
              </span>
            </div>

            <div v-if="!products.length && !productLoading" class="empty-row">暂无商品数据</div>
          </div>

          <footer class="category-footer">
            <span>共 {{ products.length }} 条记录</span>
          </footer>
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

      <div v-else-if="adminView === 'user'" class="admin-content">
        <section class="page-heading">
          <h2>用户管理</h2>
          <p>查看采购端用户、账户余额与登录状态</p>
        </section>

        <section class="category-panel">
          <div class="category-toolbar pricing-toolbar">
            <label>
              <span>关键词</span>
              <input v-model="adminUserFilters.keyword" placeholder="账号/昵称/邮箱/手机" type="text" @keyup.enter="loadAdminUsers" />
            </label>
            <label>
              <span>状态</span>
              <select v-model="adminUserFilters.status">
                <option value="">全部</option>
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </label>
            <button class="soft-action" type="button" @click="loadAdminUsers">筛选</button>
            <button class="soft-action" type="button" @click="resetAdminUserFilters">重置</button>
            <p v-if="adminUserLoading" class="toolbar-note">正在加载用户...</p>
          </div>

          <p v-if="adminUserMessage" class="category-message">{{ adminUserMessage }}</p>
          <div class="category-table" role="table" aria-label="用户列表">
            <div class="admin-user-row table-head" role="row">
              <span>用户</span>
              <span>联系方式</span>
              <span>余额</span>
              <span>状态</span>
              <span>注册信息</span>
              <span>最近登录</span>
              <span>操作</span>
            </div>
            <div v-for="user in adminUsers" :key="user.id" class="admin-user-row" role="row">
              <span class="user-cell">
                <span class="avatar mini">{{ (user.nickname || user.username).slice(0, 1).toUpperCase() }}</span>
                <span>
                  <strong>{{ user.username }}</strong>
                  <small>{{ user.nickname || '未设置昵称' }}</small>
                </span>
              </span>
              <span>
                {{ user.phone || '-' }}<br />
                <small>{{ user.email || '-' }}</small>
              </span>
              <strong>{{ formatMoney(user.balance) }}</strong>
              <span class="order-status" :class="{ success: user.status === 1, refunded: user.status === 0 }">{{ userStatusLabel(user.status) }}</span>
              <span>
                {{ formatDateTime(user.registerTime) }}<br />
                <small>{{ user.registerIp || '-' }}</small>
              </span>
              <span>{{ formatDateTime(user.lastLoginTime) }}</span>
              <span class="table-actions">
                <button class="text-action edit" type="button" @click="adjustAdminUserBalance(user)">调余额</button>
                <button class="text-action" :class="user.status === 1 ? 'danger' : 'edit'" type="button" @click="changeAdminUserStatus(user)">
                  {{ user.status === 1 ? '禁用' : '启用' }}
                </button>
              </span>
            </div>
            <div v-if="!adminUsers.length && !adminUserLoading" class="empty-row">暂无用户数据</div>
          </div>
          <footer class="table-footer">
            <span>共 {{ adminUsers.length }} 条记录</span>
          </footer>
        </section>
      </div>

      <div v-else-if="adminView === 'order'" class="admin-content admin-order-page">
        <section class="page-heading">
          <h2>订单管理</h2>
          <div class="category-toolbar admin-order-toolbar">
            <label>
              <span>订单号</span>
              <input v-model="adminOrderFilters.orderNo" placeholder="输入订单号" type="text" @keyup.enter="loadAdminOrders" />
            </label>
            <label>
              <span>商品名称</span>
              <input v-model="adminOrderFilters.productName" placeholder="输入商品名称" type="text" @keyup.enter="loadAdminOrders" />
            </label>
            <label>
              <span>充值账号</span>
              <input v-model="adminOrderFilters.rechargeAccount" placeholder="输入充值账号" type="text" @keyup.enter="loadAdminOrders" />
            </label>
            <label>
              <span>状态</span>
              <select v-model="adminOrderFilters.status">
                <option value="">全部</option>
                <option value="PENDING">待处理</option>
                <option value="PROCESSING">处理中</option>
                <option value="SUCCESS">充值成功</option>
                <option value="REFUNDED">已退款</option>
                <option value="EXCEPTION">异常</option>
              </select>
            </label>
            <label>
              <span>支付方式</span>
              <select v-model="adminOrderFilters.paymentMethod">
                <option value="">全部</option>
                <option value="BALANCE">余额支付</option>
              </select>
            </label>
            <span class="admin-order-filter-actions">
              <button class="primary-action" type="button" @click="loadAdminOrders">
                <Search :size="16" />
                筛选
              </button>
              <button class="soft-action" type="button" @click="resetAdminOrderFilters">
                <X :size="15" />
                重置
              </button>
            </span>
            <p v-if="adminOrderLoading" class="toolbar-note">正在加载订单...</p>
          </div>
          <p v-if="adminOrderMessage" class="category-message">{{ adminOrderMessage }}</p>
          <div class="category-table admin-order-panel" role="table" aria-label="订单列表">
            <div class="admin-order-table-scroll">
              <div class="admin-order-row table-head" role="row">
                <span>订单号</span>
                <span>用户</span>
                <span>商品</span>
                <span>数量/金额</span>
                <span>充值账号</span>
                <span>状态</span>
                <span>时间</span>
                <span>来源/支付</span>
                <span>渠道</span>
                <span>操作</span>
              </div>
              <div v-for="order in adminOrders" :key="order.id" class="admin-order-row" role="row">
                <strong class="order-no-cell">{{ order.orderNo }}</strong>
                <span class="admin-order-user">{{ order.username }}</span>
                <span class="buyer-product-info admin-order-product">
                  <span class="benefit-thumb small">
                    <img v-if="orderProductImage(order)" :src="orderProductImage(order)" :alt="order.productName" />
                    <Package v-else :size="18" />
                  </span>
                  <span class="template-name">
                    <strong>{{ order.productName }}</strong>
                    <small>编号：{{ order.productId }}</small>
                  </span>
                </span>
                <span class="admin-order-amount">
                  <small>{{ order.quantity }} 件</small>
                  <strong>{{ formatMoney(order.orderAmount) }}</strong>
                </span>
                <span class="admin-order-account">{{ order.rechargeAccount }}</span>
                <span class="order-status" :class="order.status.toLowerCase()">{{ orderStatusLabel(order.status) }}</span>
                <span class="admin-order-meta">
                  <span><b>下单</b>{{ formatDateTime(order.createdAt) }}</span>
                  <span><b>处理</b>{{ formatDateTime(order.processedAt || undefined) }}</span>
                  <span><b>耗时</b>{{ formatDuration(order.processingDurationSeconds) }}</span>
                </span>
                <span class="admin-order-meta">
                  <span><b>IP</b>{{ order.sourceIp || '-' }}</span>
                  <span><b>支付</b>{{ paymentMethodLabel(order.paymentMethod) }}</span>
                </span>
                <span class="admin-order-channel">
                  <strong>{{ order.channelName || '-' }}</strong>
                  <small>{{ order.channelOrderNo || order.exceptionMessage || '-' }}</small>
                </span>
                <span class="table-actions admin-order-actions">
                  <button class="text-action edit" type="button" @click="updateAdminOrderStatus(order, 'SUCCESS')">成功</button>
                  <button class="text-action edit" type="button" @click="updateAdminOrderStatus(order, 'REFUNDED')">退款</button>
                  <button class="text-action danger" type="button" @click="updateAdminOrderStatus(order, 'EXCEPTION')">异常</button>
                </span>
              </div>
            </div>
            <div v-if="!adminOrders.length && !adminOrderLoading" class="empty-row">暂无订单数据</div>
          </div>
          <footer class="table-footer">
            <span>共 {{ adminOrders.length }} 条记录</span>
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

      <div v-else-if="adminView === 'orderTemplate'" class="admin-content">
        <section class="page-heading">
          <h2>下单模板</h2>
          <p>维护用户下单时需要填写的字段组合</p>
        </section>

        <section class="category-panel">
          <div class="category-toolbar pricing-toolbar">
            <button class="primary-action" type="button" @click="openCreateOrderTemplateModal()">
              <Plus :size="18" />
              <span>添加模板</span>
            </button>
            <label>
              <span>模板名称</span>
              <input v-model="orderTemplateFilters.name" placeholder="输入名称筛选" type="text" @keyup.enter="loadOrderTemplates" />
            </label>
            <label>
              <span>状态</span>
              <select v-model="orderTemplateFilters.status">
                <option value="">全部</option>
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </label>
            <button class="soft-action" type="button" @click="loadOrderTemplates">筛选</button>
            <button class="soft-action" type="button" @click="resetOrderTemplateFilters">重置</button>
            <p v-if="orderTemplateLoading" class="toolbar-note">正在加载模板...</p>
          </div>

          <p v-if="orderTemplateMessage" class="category-message">{{ orderTemplateMessage }}</p>

          <div class="category-table" role="table" aria-label="下单模板列表">
            <div class="order-template-row table-head" role="row">
              <span>模板编号</span>
              <span>模板名称</span>
              <span>字段组合</span>
              <span>排序号</span>
              <span>状态</span>
              <span>更新时间</span>
              <span>操作</span>
            </div>

            <div v-for="template in orderTemplates" :key="template.id" class="order-template-row" role="row">
              <strong>{{ template.id }}</strong>
              <span class="template-name">
                <strong>{{ template.name }}</strong>
                <small v-if="template.description">{{ template.description }}</small>
              </span>
              <span>{{ orderTemplateFieldSummary(template) }}</span>
              <span>{{ template.sort }}</span>
              <button class="switch" :class="{ on: template.status === 1 }" type="button" @click="changeOrderTemplateStatus(template)">
                <span></span>
              </button>
              <span>{{ formatDateTime(template.updateTime || template.createTime) }}</span>
              <span class="row-actions">
                <button class="text-action edit" type="button" @click="openEditOrderTemplateModal(template)">
                  <Edit3 :size="14" />
                  编辑
                </button>
                <button class="text-action danger" type="button" @click="deleteOrderTemplate(template)">
                  <Trash2 :size="14" />
                  删除
                </button>
              </span>
            </div>

            <div v-if="!orderTemplates.length && !orderTemplateLoading" class="empty-row">暂无下单模板数据</div>
          </div>

          <footer class="category-footer">
            <span>共 {{ orderTemplates.length }} 条记录</span>
          </footer>
        </section>
      </div>

      <div v-else-if="adminView === 'supplyChannel'" class="admin-content">
        <section class="page-heading">
          <h2>货源渠道</h2>
          <p>维护第三方货源渠道账号和接口配置</p>
        </section>

        <section class="category-panel">
          <div class="category-toolbar pricing-toolbar">
            <button class="primary-action" type="button" @click="openCreateSupplyChannelModal()">
              <Plus :size="18" />
              <span>添加渠道</span>
            </button>
            <label>
              <span>渠道名称</span>
              <input v-model="supplyChannelFilters.name" placeholder="输入名称筛选" type="text" @keyup.enter="loadSupplyChannels" />
            </label>
            <label>
              <span>渠道类型</span>
              <select v-model="supplyChannelFilters.channelType">
                <option value="">全部</option>
                <option value="YOUKAYUN">优卡云</option>
              </select>
            </label>
            <label>
              <span>状态</span>
              <select v-model="supplyChannelFilters.status">
                <option value="">全部</option>
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </label>
            <button class="soft-action" type="button" @click="loadSupplyChannels">筛选</button>
            <button class="soft-action" type="button" @click="resetSupplyChannelFilters">重置</button>
            <p v-if="supplyChannelLoading" class="toolbar-note">正在加载渠道...</p>
          </div>

          <p v-if="supplyChannelMessage" class="category-message">{{ supplyChannelMessage }}</p>

          <div class="category-table" role="table" aria-label="货源渠道列表">
            <div class="supply-channel-row table-head" role="row">
              <span>编号</span>
              <span>渠道名称</span>
              <span>类型</span>
              <span>接口地址</span>
              <span>用户编号</span>
              <span>密钥</span>
              <span>余额</span>
              <span>状态</span>
              <span>操作</span>
            </div>

            <div v-for="channel in supplyChannels" :key="channel.id" class="supply-channel-row" role="row">
              <strong>{{ channel.id }}</strong>
              <span class="template-name">
                <strong>{{ channel.name }}</strong>
                <small>排序 {{ channel.sort }}</small>
              </span>
              <span>{{ supplyChannelTypeLabel(channel.channelType) }}</span>
              <span class="table-url">{{ channel.apiUrl }}</span>
              <span>{{ channel.userId }}</span>
              <span>{{ channel.secretKey }}</span>
              <span class="balance-cell">
                <strong>{{ supplyChannelBalances[channel.id] || '-' }}</strong>
                <button class="text-action edit" type="button" :disabled="queryingSupplyChannelId === channel.id" @click="querySupplyChannelBalance(channel)">
                  {{ queryingSupplyChannelId === channel.id ? '查询中...' : '查余额' }}
                </button>
              </span>
              <button class="switch" :class="{ on: channel.status === 1 }" type="button" @click="changeSupplyChannelStatus(channel)">
                <span></span>
              </button>
              <span class="row-actions">
                <button class="text-action edit" type="button" @click="openEditSupplyChannelModal(channel)">
                  <Edit3 :size="14" />
                  编辑
                </button>
                <button class="text-action danger" type="button" @click="deleteSupplyChannel(channel)">
                  <Trash2 :size="14" />
                  删除
                </button>
              </span>
            </div>

            <div v-if="!supplyChannels.length && !supplyChannelLoading" class="empty-row">暂无货源渠道数据</div>
          </div>

          <footer class="category-footer">
            <span>共 {{ supplyChannels.length }} 条记录</span>
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

        <label>
          <span>分类描述</span>
          <textarea v-model="categoryForm.description" maxlength="255" placeholder="用于前台权益中心分类卡片展示"></textarea>
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

    <div v-if="purchaseModalOpen && purchasingProduct" class="modal-mask" role="dialog" aria-modal="true">
      <section class="purchase-modal">
        <header>
          <h2>直充在线购买</h2>
          <button type="button" aria-label="关闭" @click="purchaseModalOpen = false"><X :size="18" /></button>
        </header>
        <div class="purchase-product-line">
          <span class="benefit-thumb">
            <img v-if="purchasingProduct.image" :src="purchasingProduct.image" :alt="purchasingProduct.name" />
            <Package v-else :size="24" />
          </span>
          <div>
            <strong>{{ purchasingProduct.name }}</strong>
            <small>{{ purchasingProduct.categoryName || '-' }}</small>
          </div>
          <span class="direct-tag">直充</span>
          <small>编号{{ purchasingProduct.id }}</small>
        </div>
        <dl class="purchase-summary">
          <div><dt>商品面值</dt><dd>{{ purchasingProduct.faceValue ? formatMoney(purchasingProduct.faceValue) : '-' }}</dd></div>
          <div><dt>单价</dt><dd>{{ formatMoney(purchasingProduct.salePrice) }}</dd></div>
          <div><dt>当前余额</dt><dd>{{ formatMoney(currentBalance) }}</dd></div>
        </dl>
        <label class="purchase-field">
          <span><b>*</b> 充值账号</span>
          <textarea v-model="purchaseRechargeAccount" placeholder="请输入充值账号"></textarea>
        </label>
        <div class="form-grid two">
          <label class="purchase-field">
            <span>充值数量</span>
            <input v-model.number="purchaseQuantity" :min="purchasingProduct.minPurchaseQuantity || 1" :max="purchasingProduct.maxPurchaseQuantity || undefined" type="number" />
          </label>
          <label class="purchase-field">
            <span>支付方式</span>
            <select v-model="purchasePaymentMethod">
              <option value="BALANCE">余额支付</option>
            </select>
          </label>
        </div>
        <p v-if="purchaseMessage" class="category-message">{{ purchaseMessage }}</p>
        <footer>
          <div>
            <small>共 {{ purchaseQuantity || 0 }} 件，合计支付：</small>
            <strong>{{ formatMoney(purchaseTotalAmount) }}</strong>
          </div>
          <label class="inline-check">
            <input v-model="purchaseConfirmed" type="checkbox" />
            <span>我已确认账号、购买数量、订单金额无误。</span>
          </label>
          <button class="soft-action" type="button" @click="purchaseModalOpen = false">取消</button>
          <button class="primary-action danger" type="button" :disabled="!canSubmitPurchase" @click="submitPurchase">
            {{ purchaseSubmitting ? '支付中...' : '确认支付' }}
          </button>
        </footer>
      </section>
    </div>

    <div v-if="productModalOpen" class="modal-mask" role="dialog" aria-modal="true">
      <form class="category-modal product-modal" @submit.prevent="saveProduct">
        <header>
          <h2>{{ productModalMode === 'create' ? '添加商品' : '编辑商品' }}</h2>
          <button type="button" aria-label="关闭" @click="productModalOpen = false"><X :size="18" /></button>
        </header>

        <nav class="product-modal-tabs" aria-label="商品编辑切换">
          <button class="tab-action" :class="{ active: productModalTab === 'basic' }" type="button" @click="productModalTab = 'basic'">
            基本信息
          </button>
          <button class="tab-action" :class="{ active: productModalTab === 'supply' }" type="button" @click="productModalTab = 'supply'">
            货源渠道
          </button>
        </nav>

        <div class="product-modal-body">
        <section v-show="productModalTab === 'basic'" class="product-tab-panel">
        <label>
          <span>商品名称</span>
          <input v-model="productForm.name" maxlength="100" placeholder="请输入商品名称" type="text" />
        </label>

        <label>
          <span>商品类型</span>
          <select v-model="productForm.productType">
            <option value="VIRTUAL">虚拟商品</option>
            <option value="CARD">卡密商品</option>
            <option value="NORMAL">普通商品</option>
          </select>
        </label>

        <label>
          <span>商品分类</span>
          <select v-model="productForm.categoryId">
            <option value="" disabled>请选择分类</option>
            <option v-for="item in flatCategoryOptions" :key="item.id" :value="item.id">{{ item.name }}</option>
          </select>
        </label>

        <label>
          <span>定价模板</span>
          <select v-model="productForm.pricingTemplateId">
            <option value="" disabled>请选择定价模板</option>
            <option v-for="template in pricingTemplates" :key="template.id" :value="template.id">
              {{ template.name }} · {{ formatPricingValue(template) }}
            </option>
          </select>
        </label>

        <label>
          <span>下单模板</span>
          <select v-model="productForm.orderTemplateId">
            <option value="" disabled>请选择下单模板</option>
            <option v-for="template in orderTemplates" :key="template.id" :value="template.id">
              {{ template.name }}
            </option>
          </select>
        </label>

        <div class="form-grid two">
          <label>
            <span>成本价</span>
            <input v-model.number="productForm.costPrice" min="0" step="0.01" type="number" />
          </label>
          <label>
            <span>面值/原价</span>
            <input v-model.number="productForm.faceValue" min="0" step="0.01" type="number" />
          </label>
        </div>

        <div class="sale-preview">
          <span>售价预览</span>
          <strong>{{ productSalePricePreview == null ? '-' : formatMoney(productSalePricePreview) }}</strong>
        </div>

        <label>
          <span>终端限价</span>
          <input v-model.number="productForm.terminalLimitPrice" min="0" step="0.01" placeholder="不填则前台展示 -" type="number" />
        </label>

        <label>
          <span>商品图片</span>
          <input v-model="productForm.image" placeholder="可填写图片 URL 或上传图片" />
        </label>

        <div class="icon-upload-field">
          <span class="product-thumb preview">
            <img v-if="productForm.image" :src="productForm.image" alt="" />
            <Package v-else :size="20" />
          </span>
          <label class="upload-button">
            <input accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp" type="file" :disabled="productImageUploading" @change="uploadProductImage" />
            <span>{{ productImageUploading ? '上传中...' : '上传图片' }}</span>
          </label>
          <button class="soft-action" type="button" :disabled="productImageUploading" @click="openProductMediaAssetPicker">
            选择素材
          </button>
          <button class="soft-action" type="button" :disabled="productImageUploading || !productForm.image" @click="productForm.image = ''">
            清空
          </button>
        </div>

        <div class="form-grid two">
          <label>
            <span>最小购买数量</span>
            <input v-model.number="productForm.minPurchaseQuantity" min="1" type="number" />
          </label>
          <label>
            <span>最大购买数量</span>
            <input v-model.number="productForm.maxPurchaseQuantity" min="1" placeholder="不填为不限" type="number" />
          </label>
        </div>

        <div class="form-grid two">
          <label>
            <span>排序号</span>
            <input v-model.number="productForm.sort" min="0" type="number" />
          </label>
          <label>
            <span>状态</span>
            <select v-model.number="productForm.status">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </label>
        </div>

        </section>

        <section v-show="productModalTab === 'supply'" class="supply-binding-editor product-tab-panel">
          <header>
            <h3>货源渠道商品</h3>
            <button class="add-binding-action" type="button" @click="addProductSupplyBinding">
              <Plus :size="16" />
              <span>添加绑定</span>
            </button>
            <label class="supply-cost-strategy">
              <span>渠道成本取价</span>
              <select v-model="productForm.supplyCostStrategy">
                <option value="LOWEST">使用最低价渠道</option>
                <option value="HIGHEST">使用最高价渠道</option>
              </select>
            </label>
          </header>

          <div v-for="(binding, index) in productForm.supplyBindings" :key="index" class="supply-binding-row">
            <label class="binding-field">
              <span>渠道名称</span>
              <select v-model="binding.channelId">
                <option value="" disabled>选择渠道</option>
                <option v-for="channel in supplyChannels" :key="channel.id" :value="channel.id">
                  {{ channel.name }} · {{ supplyChannelTypeLabel(channel.channelType) }}
                </option>
              </select>
            </label>
            <label class="binding-field required">
              <span>商品编号</span>
              <input v-model="binding.channelProductId" required placeholder="必填" />
            </label>
            <label class="binding-field">
              <span>商品名称</span>
              <input v-model="binding.channelProductName" placeholder="同步后填充" />
            </label>
            <label class="binding-field">
              <span>渠道成本</span>
              <input v-model.number="binding.channelCostPrice" min="0" step="0.01" type="number" placeholder="同步后填充" />
            </label>
            <label class="inline-check binding-active-check">
              <input :checked="Boolean(binding.active)" type="checkbox" @change="setActiveSupplyBinding(index, ($event.target as HTMLInputElement).checked)" />
              <span>生效</span>
            </label>
            <label class="binding-field">
              <span>状态</span>
              <select v-model.number="binding.status">
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </label>
            <label class="binding-field">
              <span>排序</span>
              <input v-model.number="binding.sort" min="0" type="number" />
            </label>
            <button class="soft-action sync-binding-action" type="button" :disabled="syncingSupplyBindingIndex === index || binding.channelId === '' || !binding.channelProductId.trim()" @click="syncProductSupplyBinding(binding, index)">
              {{ syncingSupplyBindingIndex === index ? '同步中...' : '同步' }}
            </button>
            <button class="text-action danger binding-delete-action" type="button" @click="removeProductSupplyBinding(index)">
              <Trash2 :size="14" />
            </button>
          </div>

          <p v-if="!productForm.supplyBindings.length" class="field-note">未绑定货源渠道商品时，后续订单不会自动向渠道发单。</p>
        </section>
        </div>

        <footer>
          <button class="soft-action" type="button" @click="productModalOpen = false">取消</button>
          <button class="primary-action" type="submit" :disabled="savingProduct || !productForm.name.trim()">
            {{ savingProduct ? '保存中...' : '保存' }}
          </button>
        </footer>
      </form>
    </div>

    <div v-if="orderTemplateModalOpen" class="modal-mask" role="dialog" aria-modal="true">
      <form class="category-modal order-template-modal" @submit.prevent="saveOrderTemplate">
        <header>
          <h2>{{ orderTemplateModalMode === 'create' ? '添加下单模板' : '编辑下单模板' }}</h2>
          <button type="button" aria-label="关闭" @click="orderTemplateModalOpen = false"><X :size="18" /></button>
        </header>

        <label>
          <span>模板名称</span>
          <input v-model="orderTemplateForm.name" maxlength="50" placeholder="例如：手机号+邮箱号" type="text" />
        </label>

        <div class="form-grid two">
          <label>
            <span>排序号</span>
            <input v-model.number="orderTemplateForm.sort" min="0" type="number" />
          </label>
          <label>
            <span>状态</span>
            <select v-model.number="orderTemplateForm.status">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </label>
        </div>

        <label>
          <span>备注</span>
          <textarea v-model="orderTemplateForm.description" maxlength="255" placeholder="可填写模板说明"></textarea>
        </label>

        <section class="template-field-editor">
          <header>
            <h3>下单字段</h3>
            <button class="soft-action" type="button" @click="addOrderTemplateField">添加字段</button>
          </header>

          <div v-for="(field, index) in orderTemplateForm.fields" :key="index" class="template-field-row">
            <select v-model="field.fieldType" @change="applyFieldTypeDefaults(field)">
              <option value="PHONE">手机号</option>
              <option value="QQ">QQ号</option>
              <option value="EMAIL">邮箱号</option>
              <option value="ADDRESS">收货地址</option>
              <option value="TEXT">自定义文本</option>
            </select>
            <input v-model="field.fieldName" maxlength="50" placeholder="字段名称" />
            <input v-model="field.placeholder" maxlength="255" placeholder="输入提示" />
            <label class="inline-check">
              <input v-model="field.required" type="checkbox" />
              <span>必填</span>
            </label>
            <input v-model.number="field.sort" min="0" type="number" />
            <button class="text-action danger" type="button" :disabled="orderTemplateForm.fields.length <= 1" @click="removeOrderTemplateField(index)">
              <Trash2 :size="14" />
            </button>
          </div>
        </section>

        <footer>
          <button class="soft-action" type="button" @click="orderTemplateModalOpen = false">取消</button>
          <button class="primary-action" type="submit" :disabled="savingOrderTemplate || !orderTemplateForm.name.trim()">
            {{ savingOrderTemplate ? '保存中...' : '保存' }}
          </button>
        </footer>
      </form>
    </div>

    <div v-if="supplyChannelModalOpen" class="modal-mask" role="dialog" aria-modal="true">
      <form class="category-modal supply-channel-modal" @submit.prevent="saveSupplyChannel">
        <header>
          <h2>{{ supplyChannelModalMode === 'create' ? '添加货源渠道' : '编辑货源渠道' }}</h2>
          <button type="button" aria-label="关闭" @click="supplyChannelModalOpen = false"><X :size="18" /></button>
        </header>

        <label>
          <span>渠道类型</span>
          <select v-model="supplyChannelForm.channelType">
            <option value="YOUKAYUN">优卡云</option>
          </select>
        </label>

        <label>
          <span>渠道名称</span>
          <input v-model="supplyChannelForm.name" maxlength="50" placeholder="请输入后台可识别的渠道名称" type="text" />
        </label>

        <label>
          <span>接口地址</span>
          <input v-model="supplyChannelForm.apiUrl" maxlength="500" placeholder="例如：https://api.example.com" type="text" />
        </label>

        <label>
          <span>用户编号</span>
          <input v-model="supplyChannelForm.userId" maxlength="100" placeholder="请输入用户编号" type="text" />
        </label>

        <label>
          <span>密钥</span>
          <input v-model="supplyChannelForm.secretKey" maxlength="255" :placeholder="supplyChannelModalMode === 'edit' ? '留空表示不修改密钥' : '请输入渠道密钥'" type="password" />
        </label>

        <div class="form-grid two">
          <label>
            <span>排序号</span>
            <input v-model.number="supplyChannelForm.sort" min="0" type="number" />
          </label>
          <label>
            <span>状态</span>
            <select v-model.number="supplyChannelForm.status">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </label>
        </div>

        <footer>
          <button class="soft-action" type="button" @click="supplyChannelModalOpen = false">取消</button>
          <button class="primary-action" type="submit" :disabled="savingSupplyChannel || !supplyChannelForm.name.trim()">
            {{ savingSupplyChannel ? '保存中...' : '保存' }}
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
        <button class="primary-action" type="button" @click="navigate('/index')">进入前台</button>
        <button class="soft-action" type="button" @click="navigate('/admin/index')">后台管理</button>
      </div>
    </section>
  </main>
</template>
