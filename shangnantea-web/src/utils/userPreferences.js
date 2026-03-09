/**
 * 个人设置注册表（前端编号映射）。
 * 与后端 UserPreferenceRegistry 保持一致。
 */
export const USER_PREFERENCE_CODES = {
    THEME_MODE: 101,
    FONT_SIZE: 102,
    FONT_FAMILY: 103,
    ENABLE_ANIMATION: 104,
    PROFILE_VISIBLE: 201
}

export const DEFAULT_USER_PREFERENCES = {
    themeMode: 'light',
    fontSize: 14,
    fontFamily: '',
    enableAnimation: true,
    profileVisible: true
}

/**
 * 规范化后端返回/前端编辑中的个人设置对象。
 *
 * @param {Object} source 原始对象
 * @returns {Object} 规范化后的设置对象
 */
export function normalizeUserPreferences(source = {}) {
    return {
        themeMode: source.themeMode ?? DEFAULT_USER_PREFERENCES.themeMode,
        fontSize: Number(source.fontSize ?? DEFAULT_USER_PREFERENCES.fontSize),
        fontFamily: source.fontFamily ?? DEFAULT_USER_PREFERENCES.fontFamily,
        enableAnimation: source.enableAnimation ?? DEFAULT_USER_PREFERENCES.enableAnimation,
        profileVisible: source.profileVisible ?? DEFAULT_USER_PREFERENCES.profileVisible
    }
}

/**
 * 将设置对象转换为编号驱动的接口载荷。
 *
 * @param {Object} preferences 设置对象
 * @returns {Object} 接口请求体
 */
export function buildUserPreferencePayload(preferences = {}) {
    const normalized = normalizeUserPreferences(preferences)
    return {
        items: [
            { code: USER_PREFERENCE_CODES.THEME_MODE, value: normalized.themeMode },
            { code: USER_PREFERENCE_CODES.FONT_SIZE, value: normalized.fontSize },
            { code: USER_PREFERENCE_CODES.FONT_FAMILY, value: normalized.fontFamily },
            { code: USER_PREFERENCE_CODES.ENABLE_ANIMATION, value: normalized.enableAnimation },
            { code: USER_PREFERENCE_CODES.PROFILE_VISIBLE, value: normalized.profileVisible }
        ]
    }
}
