<template>
  <div class="theme-switcher">
    <el-dropdown trigger="click" @command="handleCommand">
      <el-button circle class="theme-btn">
        <el-icon><Operation /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item 
            :command="THEMES.MINIMAL"
            :class="{ 'is-active': currentTheme === THEMES.MINIMAL }"
          >
            <el-icon><Document /></el-icon>
            <span>简约模式</span>
          </el-dropdown-item>
          <el-dropdown-item 
            :command="THEMES.TECH"
            :class="{ 'is-active': currentTheme === THEMES.TECH }"
          >
            <el-icon><Monitor /></el-icon>
            <span>科技模式</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { useThemeSystem } from '../composables/useThemeSystem'

const { currentTheme, setTheme, THEMES } = useThemeSystem()

const handleCommand = (command) => {
  setTheme(command)
}
</script>

<style scoped>
.theme-switcher {
  position: fixed;
  bottom: 100px;
  right: 50px;
  z-index: 9999;
}

.theme-btn {
  width: 50px;
  height: 50px;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-backdrop);
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-lg);
  transition: all var(--transition-base);
}

.theme-btn:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-xl);
}

.is-active {
  background: var(--color-primary);
  color: white;
}

@media (max-width: 768px) {
  .theme-switcher {
    bottom: 80px;
    right: 20px;
  }
  
  .theme-btn {
    width: 45px;
    height: 45px;
  }
}
</style>
