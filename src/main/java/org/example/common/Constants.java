package org.example.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final String FETCH_USERS_STEP = "fetchUsersStep";
    public static final String LOAD_PURCHASES_STEP = "loadPurchasesStep";
    public static final String SELECT_WINNER_STEP = "selectWinnerStep";
    public static final String GIFT_CARD_WINNER_JOB = "giftCardWinnerJob";
}

