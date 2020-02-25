package technology.bear.bot.message

import me.ivmg.telegram.extensions.filters.Filter.Custom
import me.ivmg.telegram.extensions.filters.Filter.Text
import technology.bear.constans.UserState
import technology.bear.constans.UserState.ADDING_TASK_FREQUENCY
import technology.bear.constans.UserState.ADDING_TASK_NAME


val showAllTasksFilter = Custom { text.equals("Покажи все мои цели") }

fun newTaskFilter(userStates: HashMap<Long, UserState>): Custom {
    return Custom {
        userStates[chat.id] != ADDING_TASK_NAME
                && userStates[chat.id] != ADDING_TASK_FREQUENCY
                && text.equals("Добавь периодическую цель")
    }
}

fun taskFrequencyFilter(userStates: HashMap<Long, UserState>) =
    Text and Custom { userStates[chat.id] == ADDING_TASK_FREQUENCY }

fun taskNameFilter(userStates: HashMap<Long, UserState>) =
    Text and Custom { userStates[chat.id] == ADDING_TASK_NAME }
