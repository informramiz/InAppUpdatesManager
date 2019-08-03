package github.informramiz.inappupdatesmanager.base


/**
 * Created by Ramiz Raja on 2019-05-30.
 */
enum class InAppUpdateEvent {
    PROMPT_USER_TO_UPDATE,
    ERROR_UPDATE_TYPE_NOT_ALLOWED,
    ERROR_IMMEDIATE_UPDATE_CANCELLED_BY_USER,
    ERROR_FLEXIBLE_UPDATE_CANCELLED_BY_USER,
    ERROR_IMMEDIATE_UPDATE_FAILED,
    ERROR_FLEXIBLE_UPDATE_FAILED
}