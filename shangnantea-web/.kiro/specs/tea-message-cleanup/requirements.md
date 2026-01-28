# Requirements Document: 茶叶模块消息系统TODO标记清理

## Introduction

茶叶模块在之前的消息系统重构中已经完成了代码迁移，使用了新的 `showByCode(response.code)` 统一消息系统。但是在迁移过程中，旧的消息调用代码和TODO标记没有被清理，导致代码中同时存在新旧两套消息调用，以及大量过时的TODO注释。

本需求旨在清理这些过时的TODO标记和冗余的旧消息调用代码，确保代码库的整洁性和一致性。

## Glossary

- **TODO标记**: 代码中的 `// TODO: 迁移到新消息系统` 注释
- **旧消息调用**: 使用 `teaMessages.success.showXxx()` 或 `teaMessages.error.showXxx()` 的代码
- **新消息系统**: 使用 `showByCode(response.code)` 的统一消息系统
- **冗余代码**: 在新消息系统已经生效的情况下，仍然存在的旧消息调用

## Requirements

### Requirement 1: 清理TeaManagePage.vue中的过时TODO标记和冗余代码

**User Story:** 作为开发者，我希望清理TeaManagePage.vue中的过时TODO标记和冗余的旧消息调用，以保持代码整洁和一致性。

#### Acceptance Criteria

1. THE System SHALL remove all TODO comments that reference "迁移到新消息系统" in TeaManagePage.vue
2. THE System SHALL remove all redundant old message calls (teaMessages.success/error.showXxx()) that are duplicated by showByCode() calls
3. THE System SHALL preserve all showByCode() calls as they are the correct implementation
4. THE System SHALL preserve all teaMessages.prompt.showXxx() calls as they are for frontend validation (not API responses)
5. WHEN reviewing the handleToggleStatus method, THE System SHALL ensure only showByCode(response.code) remains for API response handling
6. WHEN reviewing batch operation methods, THE System SHALL ensure only showByCode(response.code) remains for API response handling
7. WHEN reviewing the handleDelete method, THE System SHALL ensure only showByCode(response.code) remains for API response handling
8. WHEN reviewing the handleSave method, THE System SHALL ensure only showByCode(response.code) remains for API response handling
9. WHEN reviewing category management methods, THE System SHALL ensure only showByCode(response.code) remains for API response handling

### Requirement 2: 清理TeaDetailPage.vue中的过时TODO标记和冗余代码

**User Story:** 作为开发者，我希望清理TeaDetailPage.vue中的过时TODO标记和冗余的旧消息调用，以保持代码整洁和一致性。

#### Acceptance Criteria

1. THE System SHALL remove all TODO comments that reference "迁移到新消息系统" in TeaDetailPage.vue
2. THE System SHALL remove all redundant old message calls (teaMessages.success/error.showXxx()) that are duplicated by showByCode() calls
3. THE System SHALL preserve all showByCode() calls as they are the correct implementation
4. THE System SHALL preserve all teaMessages.prompt.showXxx() calls as they are for frontend validation (not API responses)
5. WHEN reviewing the submitReply method, THE System SHALL ensure only showByCode(response.code) remains for API response handling
6. WHEN reviewing the handleLikeReview method, THE System SHALL ensure only showByCode(response.code) remains for API response handling
7. WHEN reviewing the toggleFavorite method, THE System SHALL ensure only showByCode(response.code) remains for API response handling
8. WHEN reviewing the loadTeaDetail method, THE System SHALL ensure only showByCode(response.code) remains for API response handling
9. WHEN reviewing the addToCart method, THE System SHALL ensure only showByCode(response.code) remains for API response handling
10. WHEN reviewing the buyNow method, THE System SHALL verify it only contains router navigation (no API call, so no message handling needed)

### Requirement 3: 清理TeaListPage.vue中的过时TODO标记和冗余代码

**User Story:** 作为开发者，我希望清理TeaListPage.vue中的过时TODO标记和冗余的旧消息调用，以保持代码整洁和一致性。

#### Acceptance Criteria

1. THE System SHALL remove all TODO comments that reference "迁移到新消息系统" in TeaListPage.vue
2. THE System SHALL remove all redundant old message calls (teaMessages.success/error.showXxx()) that are duplicated by showByCode() calls
3. THE System SHALL preserve all showByCode() calls as they are the correct implementation
4. THE System SHALL preserve all teaMessages.prompt.showXxx() calls as they are for frontend validation (not API responses)
5. WHEN reviewing the loadTeas method, THE System SHALL ensure only showByCode(response.code) remains for API response handling

### Requirement 4: 验证清理后的代码符合消息系统规范

**User Story:** 作为开发者，我希望验证清理后的代码完全符合消息系统使用指南的规范，确保没有遗漏或错误。

#### Acceptance Criteria

1. THE System SHALL verify that all API response handling uses showByCode(response.code)
2. THE System SHALL verify that all frontend validation uses teaMessages.prompt.showXxx()
3. THE System SHALL verify that no TODO comments referencing "迁移到新消息系统" remain
4. THE System SHALL verify that no redundant old message calls (teaMessages.success/error.showXxx()) remain
5. THE System SHALL verify that the code follows the message system guide at shangnantea-web/docs/tasks/message-system-guide.md

### Requirement 5: 文档更新

**User Story:** 作为开发者，我希望在清理完成后更新相关文档，记录清理的内容和结果。

#### Acceptance Criteria

1. THE System SHALL document the number of TODO markers removed
2. THE System SHALL document the number of redundant old message calls removed
3. THE System SHALL document which files were cleaned
4. THE System SHALL verify that no functional changes were made (only cleanup)
