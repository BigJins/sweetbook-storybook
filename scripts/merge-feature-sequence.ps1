param(
    [string]$BackendBranch = "feature/backend-ai",
    [string]$FrontendBranch = "feature/frontend-ui",
    [switch]$Push
)

$ErrorActionPreference = "Stop"

function Invoke-Step {
    param(
        [string]$Label,
        [scriptblock]$Action
    )

    Write-Host ""
    Write-Host "==> $Label" -ForegroundColor Cyan
    & $Action
}

function Assert-CleanWorktree {
    $status = git status --porcelain
    if ($LASTEXITCODE -ne 0) {
        throw "git status failed."
    }
    if ($status) {
        throw "Working tree is not clean. Commit or stash changes before running this script."
    }
}

function Assert-BranchExists {
    param([string]$BranchName)

    git show-ref --verify --quiet ("refs/heads/" + $BranchName)
    if ($LASTEXITCODE -ne 0) {
        throw "Local branch '$BranchName' does not exist."
    }
}

function Ensure-FrontendDependencies {
    Push-Location "frontend"
    try {
        if (Test-Path "package-lock.json") {
            npm ci
        } else {
            npm install
        }
        if ($LASTEXITCODE -ne 0) {
            throw "Frontend dependency install failed."
        }
    }
    finally {
        Pop-Location
    }
}

function Run-BackendTests {
    Push-Location "backend"
    try {
        .\mvnw.cmd test
        if ($LASTEXITCODE -ne 0) {
            throw "Backend tests failed."
        }
    }
    finally {
        Pop-Location
    }
}

function Run-FrontendChecks {
    Push-Location "frontend"
    try {
        npm test -- --run
        if ($LASTEXITCODE -ne 0) {
            throw "Frontend tests failed."
        }

        npm run build
        if ($LASTEXITCODE -ne 0) {
            throw "Frontend build failed."
        }
    }
    finally {
        Pop-Location
    }
}

Invoke-Step "Checking repository state" {
    Assert-CleanWorktree
    Assert-BranchExists -BranchName $BackendBranch
    Assert-BranchExists -BranchName $FrontendBranch
    git branch --show-current
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to determine current branch."
    }
}

Invoke-Step "Fetching latest refs" {
    git fetch --all --prune
    if ($LASTEXITCODE -ne 0) {
        throw "git fetch failed."
    }
}

Invoke-Step "Merging backend branch" {
    git merge --no-ff $BackendBranch
    if ($LASTEXITCODE -ne 0) {
        throw "Backend merge failed."
    }
}

Invoke-Step "Running backend tests" {
    Run-BackendTests
}

Invoke-Step "Merging frontend branch" {
    git merge --no-ff $FrontendBranch
    if ($LASTEXITCODE -ne 0) {
        throw "Frontend merge failed."
    }
}

Invoke-Step "Installing frontend dependencies" {
    Ensure-FrontendDependencies
}

Invoke-Step "Running frontend checks" {
    Run-FrontendChecks
}

if ($Push) {
    Invoke-Step "Pushing main" {
        git push origin HEAD
        if ($LASTEXITCODE -ne 0) {
            throw "git push failed."
        }
    }
}

Write-Host ""
Write-Host "Sequence completed successfully." -ForegroundColor Green
Write-Host "Backend merged and tested, frontend merged and build/test checked." -ForegroundColor Green
