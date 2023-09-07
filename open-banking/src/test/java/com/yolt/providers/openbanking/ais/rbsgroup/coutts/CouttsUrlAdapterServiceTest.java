package com.yolt.providers.openbanking.ais.rbsgroup.coutts;

import com.yolt.providers.openbanking.ais.rbsgroup.coutts.service.ais.fetchdataservice.PortRemovalUrlAdapter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CouttsUrlAdapterServiceTest {


    PortRemovalUrlAdapter urlAdapter = new PortRemovalUrlAdapter();

    @Test
    public void shouldRemovePortNumberFromUrl() {
        //given
        String urlWithPort = "https://api.coutts.com:8444/open-banking/v3.1/aisp/accounts/accountId:111111/transactions?fromBookingDateTime=2019-08-21&offset=100&limit=100";
        String expectedUrl = "https://api.coutts.com/open-banking/v3.1/aisp/accounts/accountId:111111/transactions?fromBookingDateTime=2019-08-21&offset=100&limit=100";
        //when
        String returnedUrl = urlAdapter.removePortNumberFromUrl(urlWithPort);
        //then
        assertThat(returnedUrl).isEqualTo(expectedUrl);
    }

    @Test
    public void shouldKeepUrlUntouchedWhenThereIsNoPortNumberInUrl() {
        //given
        String urlWithoutPortAndExpectedUrl = "https://api.coutts.com/open-banking/v3.1/aisp/accounts/accountId:111111/transactions?fromBookingDateTime=2019-08-21&offset=100&limit=100";
        //when
        String returnedUrl = urlAdapter.removePortNumberFromUrl(urlWithoutPortAndExpectedUrl);
        //then
        assertThat(returnedUrl).isEqualTo(urlWithoutPortAndExpectedUrl);
    }

    @Test
    public void shouldReturnEmptyStringWhenEmptyStringIsGiven() {
        //given
        String emptyString = "";
        //when
        String returnedUrl = urlAdapter.removePortNumberFromUrl(emptyString);
        //then
        assertThat(returnedUrl).isEmpty();

    }
}
