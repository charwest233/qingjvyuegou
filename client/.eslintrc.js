module.exports = {
  env: {
    browser: true,
    es2021: true,
    node: true
  },
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-essential'
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module'
  },
  plugins: [
    'vue'
  ],
  rules: {
    // 禁用 v-model 参数检查，因为这是 Vue 3 的标准语法
    'vue/no-v-model-argument': 'off',
    'vue/multi-word-component-names': 'off'
  }
}
