DELETE FROM VERIFICATION_TOKEN;
DELETE FROM PERSON;
DELETE FROM ROLE;

INSERT INTO public.role(
            role_id, name)
    VALUES (100, 'ROLE_EMPLOYEE');

INSERT INTO public.person(
            person_id, first_name, last_name, email, password, role_id, enabled)
    VALUES (100, 'Anatolii', 'Syvenko', 'a.p.syvenko@gmail.com', 'password', 100, FALSE );

INSERT INTO public.person(
            person_id, first_name, last_name, email, password, role_id, enabled)
    VALUES (101, 'Enabled', 'Person', 'enabled@mail.com', 'password', 100, TRUE );

INSERT INTO public.verification_token(
            verification_token_id, token, employee_id, date_expired)
    VALUES (100, 'TEST_TOKEN', 100, now());