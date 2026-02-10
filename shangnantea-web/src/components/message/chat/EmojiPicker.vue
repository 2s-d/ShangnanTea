<template>
  <div class="emoji-picker">
    <div class="emoji-container">
      <span 
        v-for="emoji in emojiList" 
        :key="emoji"
        class="emoji-item"
        @click="$emit('select', emoji)">
        {{ emoji }}
      </span>
    </div>
  </div>
</template>

<script setup>
defineOptions({
  name: 'EmojiPicker'
})

defineEmits(['select', 'close'])

const emojiList = [
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇', 
  '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚', 
  '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩',
  '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣',
  '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬',
  '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗',
  '👋', '👍', '👎', '❤️', '💋', '👏', '🙏', '🤝', '💪', '✌️'
]
</script>

<style lang="scss" scoped>
.emoji-picker {
  background-color: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .emoji-container {
    display: flex;
    flex-wrap: wrap;
    max-width: 400px;
    max-height: 200px;
    overflow-y: auto;
    
    .emoji-item {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      font-size: 20px;
      border-radius: 4px;
      transition: background-color 0.2s;
      
      &:hover {
        background-color: #f0f0f0;
      }
    }
  }
}
</style>