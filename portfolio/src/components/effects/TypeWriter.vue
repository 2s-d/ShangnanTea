<template>
  <span class="typewriter">
    {{ displayText }}
    <span class="cursor" v-if="showCursor">|</span>
  </span>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  text: {
    type: String,
    required: true
  },
  speed: {
    type: Number,
    default: 100
  },
  delay: {
    type: Number,
    default: 0
  }
})

const displayText = ref('')
const showCursor = ref(true)

onMounted(() => {
  setTimeout(() => {
    let index = 0
    const type = () => {
      if (index < props.text.length) {
        displayText.value += props.text.charAt(index)
        index++
        setTimeout(type, props.speed)
      } else {
        // 打字完成后，光标闪烁一会儿再消失
        setTimeout(() => {
          showCursor.value = false
        }, 2000)
      }
    }
    type()
  }, props.delay)
})
</script>

<style scoped>
.typewriter {
  display: inline-block;
}

.cursor {
  animation: blink 1s step-end infinite;
  margin-left: 2px;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
</style>
