package com.netcracker.service.report;

import com.netcracker.exception.CurrentUserNotPresentException;
import com.netcracker.model.dto.CalendarItemDto;
import com.netcracker.model.entity.Person;
import com.netcracker.repository.data.interfaces.PersonRepository;
import com.netcracker.repository.data.interfaces.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.netcracker.util.MessageConstant.USER_ERROR_NOT_PRESENT;

/**
 * Created by nuts on 3/19/17.
 */
@Service
public class CalendarServiceImpl implements CalendarService {
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private PersonRepository personRepository;

    @Override
    public List<CalendarItemDto> getDataByPeriod(Timestamp start, Timestamp end, String email) throws CurrentUserNotPresentException {
        Locale locale =  LocaleContextHolder.getLocale();
        Person person = personRepository.findPersonByEmail(email).orElseThrow(() ->
                new CurrentUserNotPresentException(messageSource.getMessage(USER_ERROR_NOT_PRESENT, null, locale)));

        return requestRepository.getRequestsByEmployeeAndPeriod(start, end, person)
                .stream()
                .map(CalendarItemDto::new)
                .collect(Collectors.toList());
    }
}
