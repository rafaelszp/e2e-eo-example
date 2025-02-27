FROM rabbitmq:4-management

COPY ./rabbitmq_delayed_message_exchange-4.0.2.ez /plugins/

RUN rabbitmq-plugins enable rabbitmq_delayed_message_exchange
RUN rabbitmqctl enable_feature_flag all

