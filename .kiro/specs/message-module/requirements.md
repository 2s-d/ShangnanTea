# Requirements Document

## Introduction

商南茶城消息模块为用户提供完整的消息通信和通知功能，包括私信聊天、系统通知、消息管理等核心功能。该模块采用Spring Boot + MyBatis架构，遵循统一的Result返回格式和状态码规范。

## Glossary

- **Message_System**: 消息系统，负责处理所有消息相关的业务逻辑
- **Chat_Session**: 聊天会话，用户之间的私信对话容器
- **Chat_Message**: 聊天消息，会话中的具体消息内容
- **System_Notification**: 系统通知，由系统发送给用户的通知消息
- **User_Notification**: 用户通知，用户收到的所有类型通知的统称
- **Message_Status**: 消息状态，包括未读(0)和已读(1)
- **Notification_Settings**: 通知设置，用户对各类通知的接收偏好配置
- **Admin_User**: 管理员用户，具有发送系统通知和广播消息权限的用户
- **Broadcast_Message**: 广播消息，发送给所有用户的消息
- **Image_Message**: 图片消息，包含图片内容的聊天消息
- **Unread_Count**: 未读数量，用户未读消息的统计数量
- **Message_Statistics**: 消息统计，管理员查看的消息数据统计信息

## Requirements

### Requirement 1: 基础消息管理

**User Story:** As a user, I want to manage my messages, so that I can view, read, and delete messages efficiently.

#### Acceptance Criteria

1. WHEN a user requests the message list, THE Message_System SHALL return paginated messages with sender information and read status
2. WHEN a user requests message details by ID, THE Message_System SHALL return the complete message content and mark it as read
3. WHEN a user sends a message to another user, THE Message_System SHALL create the message record and update the recipient's unread count
4. WHEN a user marks messages as read, THE Message_System SHALL update the message status to read (1) and decrease the unread count
5. WHEN a user deletes messages, THE Message_System SHALL remove the specified messages from the user's message list
6. WHEN querying messages, THE Message_System SHALL only return messages belonging to the current user

### Requirement 2: 未读消息统计

**User Story:** As a user, I want to see my unread message count, so that I know when I have new messages to check.

#### Acceptance Criteria

1. WHEN a user requests unread count, THE Message_System SHALL return the total number of unread messages
2. WHEN a new message is sent to a user, THE Message_System SHALL increment the recipient's unread count
3. WHEN a user marks messages as read, THE Message_System SHALL decrement the unread count accordingly
4. THE Message_System SHALL calculate unread count in real-time based on message status

### Requirement 3: 聊天会话管理

**User Story:** As a user, I want to manage chat sessions, so that I can organize my conversations with different users.

#### Acceptance Criteria

1. WHEN a user requests chat sessions, THE Message_System SHALL return all sessions with the latest message preview and unread count
2. WHEN a user creates a chat session with another user, THE Message_System SHALL create a new session or return the existing session
3. WHEN a user requests chat messages for a session, THE Message_System SHALL return paginated messages in chronological order
4. WHEN a user deletes a chat session, THE Message_System SHALL remove the session and all associated messages
5. WHEN displaying sessions, THE Message_System SHALL sort them by the latest message timestamp in descending order
6. THE Message_System SHALL prevent users from accessing sessions they don't participate in

### Requirement 4: 聊天消息发送

**User Story:** As a user, I want to send text and image messages in chat sessions, so that I can communicate effectively with other users.

#### Acceptance Criteria

1. WHEN a user sends a text message in a session, THE Message_System SHALL create the message record and update the session's last message
2. WHEN a user sends an image message, THE Message_System SHALL upload the image file and create a message record with the image URL
3. WHEN sending a message, THE Message_System SHALL validate that the user is a participant in the session
4. WHEN a message is sent, THE Message_System SHALL update the recipient's unread count for that session
5. THE Message_System SHALL support image formats: JPG, PNG, GIF with maximum size of 5MB
6. WHEN image upload fails, THE Message_System SHALL return an appropriate error code

### Requirement 5: 系统通知管理

**User Story:** As a user, I want to receive and manage system notifications, so that I stay informed about important events.

#### Acceptance Criteria

1. WHEN a user requests notifications, THE Message_System SHALL return paginated notifications with read status
2. WHEN a user marks a notification as read, THE Message_System SHALL update the notification status to read (1)
3. WHEN a user deletes a notification, THE Message_System SHALL remove the notification from the user's list
4. WHEN a user requests notification settings, THE Message_System SHALL return the user's notification preferences
5. THE Message_System SHALL support different notification types: system, order, forum, message
6. WHEN displaying notifications, THE Message_System SHALL sort them by creation time in descending order

### Requirement 6: 通知设置管理

**User Story:** As a user, I want to configure my notification preferences, so that I only receive notifications I'm interested in.

#### Acceptance Criteria

1. WHEN a user updates notification settings, THE Message_System SHALL save the preferences for each notification type
2. WHEN a user tests notifications, THE Message_System SHALL send a test notification to verify the settings
3. THE Message_System SHALL support enabling/disabling notifications for: system messages, order updates, forum replies, private messages
4. WHEN notification settings are updated, THE Message_System SHALL validate the input data
5. THE Message_System SHALL apply notification settings immediately after update

### Requirement 7: 管理员消息功能

**User Story:** As an administrator, I want to manage messages and send notifications, so that I can communicate with users effectively.

#### Acceptance Criteria

1. WHEN an administrator requests the message list, THE Message_System SHALL return all messages with filtering and pagination
2. WHEN an administrator sends a system notification, THE Message_System SHALL create notification records for specified users
3. WHEN an administrator broadcasts a message, THE Message_System SHALL send the message to all users in the system
4. WHEN an administrator requests message statistics, THE Message_System SHALL return aggregated data including total messages, unread count, and daily statistics
5. THE Message_System SHALL verify administrator role before allowing admin operations
6. WHEN broadcasting, THE Message_System SHALL handle large user volumes efficiently

### Requirement 8: 权限验证与安全

**User Story:** As a system architect, I want strict permission controls, so that users can only access their own messages and authorized content.

#### Acceptance Criteria

1. WHEN a user accesses messages, THE Message_System SHALL verify the user is logged in
2. WHEN a user accesses a specific message or session, THE Message_System SHALL verify ownership or participation
3. WHEN an admin operation is requested, THE Message_System SHALL verify the user has administrator role
4. THE Message_System SHALL prevent unauthorized access to other users' messages
5. THE Message_System SHALL use UserContext to retrieve the current user ID securely
6. WHEN authentication fails, THE Message_System SHALL return appropriate error codes (7100-7142)

### Requirement 9: 数据一致性与事务管理

**User Story:** As a developer, I want transactional operations, so that data remains consistent during complex operations.

#### Acceptance Criteria

1. WHEN deleting a chat session, THE Message_System SHALL delete both the session and all associated messages in a single transaction
2. WHEN sending a message, THE Message_System SHALL update the message record and unread count atomically
3. WHEN marking messages as read, THE Message_System SHALL update message status and unread count atomically
4. IF any operation in a transaction fails, THE Message_System SHALL roll back all changes
5. THE Message_System SHALL use @Transactional annotation for multi-step operations

### Requirement 10: 状态码规范

**User Story:** As a frontend developer, I want consistent status codes, so that I can handle responses uniformly.

#### Acceptance Criteria

1. THE Message_System SHALL use status codes in range 7000-7021 for successful operations
2. THE Message_System SHALL use status codes in range 7100-7142 for failed operations
3. THE Message_System SHALL return HTTP 200 for all responses, with business status in the code field
4. WHEN an operation succeeds, THE Message_System SHALL return Result.success(code, data)
5. WHEN an operation fails, THE Message_System SHALL return Result.failure(code)
6. THE Message_System SHALL follow the status code mapping defined in code-message-mapping.md

### Requirement 11: 分页与查询

**User Story:** As a user, I want paginated message lists, so that I can browse messages efficiently.

#### Acceptance Criteria

1. WHEN requesting message lists, THE Message_System SHALL support pagination with pageNum and pageSize parameters
2. WHEN requesting chat messages, THE Message_System SHALL return messages in chronological order with pagination
3. THE Message_System SHALL return total count along with paginated results
4. WHEN pageNum or pageSize is invalid, THE Message_System SHALL use default values (pageNum=1, pageSize=10)
5. THE Message_System SHALL support filtering messages by status (read/unread) and type

### Requirement 12: 文件上传处理

**User Story:** As a user, I want to send image messages, so that I can share visual content in conversations.

#### Acceptance Criteria

1. WHEN uploading an image, THE Message_System SHALL use FileUploadUtils.uploadImage() for file handling
2. WHEN an image is uploaded, THE Message_System SHALL validate file type (JPG, PNG, GIF)
3. WHEN an image is uploaded, THE Message_System SHALL validate file size (maximum 5MB)
4. WHEN upload succeeds, THE Message_System SHALL return the image URL
5. WHEN upload fails, THE Message_System SHALL return an appropriate error code
6. THE Message_System SHALL store image URLs in the message content field
