package com.yolt.providers.knabgroup.common.dto.internal;

import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FetchDataResultV2Test {

    private FetchDataResultV2 fetchDataResult = new FetchDataResultV2();

    @Test
    public void shouldInitializeFieldsOnObjectInitialization() {
        //when
        List<ProviderAccountDTO> responseAccounts = fetchDataResult.getResponseAccounts();

        //then
        assertThat(responseAccounts).isNotNull();
    }

    @Test
    void shouldReturnProperFetchedAccounts() {
        //given
        ProviderAccountDTO mockedAccount1 = mock(ProviderAccountDTO.class);
        ProviderAccountDTO mockedAccount2 = mock(ProviderAccountDTO.class);
        fetchDataResult.addFetchedAccount(mockedAccount1);
        fetchDataResult.addFetchedAccount(mockedAccount2);

        //when
        List<ProviderAccountDTO> result = fetchDataResult.getResponseAccounts();

        //then
        assertThat(result).containsExactlyInAnyOrder(mockedAccount1, mockedAccount2);
    }
}
