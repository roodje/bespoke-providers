package com.yolt.providers.abnamrogroup.common.data;

import com.yolt.providers.abnamro.dto.TransactionResponseTransactions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the counter party name from the ABN AMRO transaction. Often the full counter party name can only be
 * extracted from the description lines. Check the sample data in transaction-prd-response-anonymized.json to
 * get an idea of the transactions ABN provides.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class AbnAmroCounterPartyNameExtractor {

    private static final Pattern PATTERN = Pattern.compile("^(.*),PAS(\\d+)(.*)");
    private static final String COUNTER_PARTY_NAME_PREFIX = "Naam:";

    static String extractCounterPartyName(TransactionResponseTransactions transaction) {
        String counterParty = tryNaamFieldInDescriptionLines(transaction);
        if (counterParty == null) {
            counterParty = tryCounterPartyNameField(transaction);
        }
        if (counterParty == null) {
            counterParty = trySecondDescriptionLine(transaction);
        }

        return counterParty;
    }

    private static String tryCounterPartyNameField(TransactionResponseTransactions transaction) {
        return StringUtils.isBlank(transaction.getCounterPartyName()) ? null : transaction.getCounterPartyName();
    }

    /**
     * If the transaction has description lines, this is the most effective way of getting the counter party
     * name, because descripion lines will contain the full counter party names, potentially split across two
     * lines, so we have to try to look ahead to see if there is a second part to the counter party name.
     *
     * @param transaction
     * @return
     */
    private static String tryNaamFieldInDescriptionLines(TransactionResponseTransactions transaction) {

        // Get the first part of the counter party name from the "Naam: " description line
        String firstPart = null;
        int indexOfPotentialSecondPart = -1;

        if (transaction.getDescriptionLines() != null) {
            for (int i = 0; i < transaction.getDescriptionLines().size(); i++) {
                String descriptionLine = transaction.getDescriptionLines().get(i);
                if (descriptionLine.startsWith(COUNTER_PARTY_NAME_PREFIX)) {
                    firstPart = descriptionLine.replace(COUNTER_PARTY_NAME_PREFIX, "").trim();
                    indexOfPotentialSecondPart = i + 1;
                    break;
                }
            }
        }

        // If no "Naam: " line is present.. stop
        if (StringUtils.isBlank(firstPart)) {
            return null;
        }

        // Check if there is a second part
        String potentialSecondPartOfCounterPartyName = tryGetSecondPartOfCounterPartyName(transaction, indexOfPotentialSecondPart);

        return firstPart + (potentialSecondPartOfCounterPartyName != null ? potentialSecondPartOfCounterPartyName : "");
    }

    /**
     * Sometimes the counter party name found in the 'Naam:' description line(s) has a second part on the next line.
     *
     * @param transaction
     * @param index
     * @return
     */
    private static String tryGetSecondPartOfCounterPartyName(TransactionResponseTransactions transaction, int index) {

        // If the next line doesn't exist
        if (index >= transaction.getDescriptionLines().size()) {
            return null;
        }

        // If the next line is a new field...
        String nextLine = transaction.getDescriptionLines().get(index);
        if (isNewFieldLine(nextLine)) {
            return null;
        }

        return nextLine;
    }

    /**
     * Sometimes the counter party name can be extracted from the second description line, which then ends with
     * PASxxx.
     *
     * @param transaction
     * @return
     */
    private static String trySecondDescriptionLine(TransactionResponseTransactions transaction) {
        if (!hasAtLeastTwoDescriptionLines(transaction)) {
            return null;
        }
        String secondDescriptionLine = transaction.getDescriptionLines().get(1);
        Matcher matcher = PATTERN.matcher(secondDescriptionLine);
        if (matcher.matches()) {
            return secondDescriptionLine.replaceFirst(",PAS(\\d{1,5})(.*)$", "");
        }
        return null;
    }

    private static boolean isNewFieldLine(String line) {
        return line.matches("^(\\w+)?[\\s]?:[\\s]?(.*)");
    }

    private static boolean hasAtLeastTwoDescriptionLines(TransactionResponseTransactions transaction) {
        return transaction.getDescriptionLines() != null && transaction.getDescriptionLines().size() >= 2;
    }
}
