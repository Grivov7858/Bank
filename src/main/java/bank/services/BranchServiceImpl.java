package bank.services;

import bank.domain.dtos.BranchImportDto;
import bank.domain.entities.Branch;
import bank.repositories.BranchRepository;
import bank.util.FileUtil;
import bank.util.ValidationUtil;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BranchServiceImpl implements BranchService {
    private final static String BRANCHES_JSON_FILE_PATH =
            "D:\\Java\\bank\\src\\main\\resources\\files\\json\\branches.json";
    private final BranchRepository branchRepository;
    private final FileUtil fileUtil;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    @Autowired
    public BranchServiceImpl(BranchRepository branchRepository, FileUtil fileUtil, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.branchRepository = branchRepository;
        this.fileUtil = fileUtil;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean branchesAreImported() {
        return this.branchRepository.count() != 0;
    }

    @Override
    public String readBranchesJsonFile() throws IOException {
        String branchesFileContent = this.fileUtil.readFile(BRANCHES_JSON_FILE_PATH);
        return branchesFileContent;
    }

    @Override
    public String importBranches(String branchesJson) {
        StringBuilder importResult = new StringBuilder();
        BranchImportDto[] branchImportDtos = this.gson.fromJson(branchesJson, BranchImportDto[].class);

        for (BranchImportDto branchImportDto: branchImportDtos) {
            if (!validationUtil.isValid(branchImportDto)) {
                importResult.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Branch branch = this.modelMapper.map(branchImportDto, Branch.class);

            this.branchRepository.saveAndFlush(branch);
            importResult.append(String
                    .format("Successfully imported Branch - %s", branch.getName()))
                    .append(System.lineSeparator());
        }

        return importResult.toString().trim();
    }
}














