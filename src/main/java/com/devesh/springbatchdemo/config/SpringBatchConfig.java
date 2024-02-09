package com.devesh.springbatchdemo.config;

import com.devesh.springbatchdemo.Repository.CustomerRepository;
import com.devesh.springbatchdemo.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SpringBatchConfig {

    @Autowired
    private CustomerRepository customerRepository;

    //Creating an ItemReader Bean
    @Bean
    public FlatFileItemReader<Customer> getItemReaderBean() {
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(createLineMapper());
        return itemReader;
    }

    //Creating Line Mapper: Line Mapper Reads CSV file and maps to Customer Object
    private LineMapper<Customer> createLineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        //lineTokenizer will extract value from CSV File
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

        //fieldSetMapper is used to do mapping to Customer class
        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

    //Creating ItemProcessor Bean
    @Bean
    public CustomerProcessor getItemProcessorBean() {
        return new CustomerProcessor();
    }

    //Creating ItemWriter Bean
    @Bean
    public RepositoryItemWriter<Customer> getItemWriterBean() {
        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }

    //Creating a Step bean here by using ItemReader, ItemProcessor & ItemWriter Beans
    @Bean
    public Step createStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        Step step =  new StepBuilder("csv-step", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(getItemReaderBean())
                .processor(getItemProcessorBean())
                .writer(getItemWriterBean())
                .taskExecutor(getTaskExecutorBean())
                .build();
        return step;
    }

    //Creating Job beans by using the Step Bean. We can give a job multiple steps
    @Bean
    public Job createJobBean(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("importCustomers", jobRepository)
                .flow(createStep1(jobRepository, transactionManager))
//                .next(getStep2(jobRepository, transactionManager))
                .end().build();
    }


    //By Default Spring Batch is Synchronous
    //We can do it asynchronously and concurrently as given below
    @Bean
    public TaskExecutor getTaskExecutorBean() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        //setting 10 threads to run concurrently
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }
}
