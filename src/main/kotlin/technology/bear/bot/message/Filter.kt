package technology.bear.bot.message

import me.ivmg.telegram.extensions.filters.Filter.Custom
import me.ivmg.telegram.extensions.filters.Filter.Text
import technology.bear.constans.UserState
import technology.bear.constans.UserState.*


val showAllTasksFilter = Custom { text.equals("Покажи все мои цели") }

val removeTasksFilter = Custom { text.equals("Удалить цель") }

fun newTaskFilter(userStates: Map<Long, UserState>): Custom {
    return Custom {
        userStates[chat.id] != ADDING_TASK_NAME
                && userStates[chat.id] != ADDING_TASK_FREQUENCY
                && text.equals("Добавь цель")
    }
}

fun taskFrequencyFilter(userStates: Map<Long, UserState>) =
    Text and Custom { userStates[chat.id] == ADDING_TASK_FREQUENCY }

fun taskNameFilter(userStates: Map<Long, UserState>) =
    Text and Custom { userStates[chat.id] == ADDING_TASK_NAME }

fun removingTaskNameFilter(userStates: Map<Long, UserState>) =
    Text and Custom { userStates[chat.id] == REMOVING_TASK }
