echo Click to start
pause
@echo off
for /f %%f in ('dir /b .') do (
  echo.%%f|findstr /C:".html" >nul 2>&1 && (
    
    echo Found %%~nf
 pandoc.exe --eol=native --wrap=none -f html -t markdown %%f -o %%~nf.md
    
  ) || echo Noghght found %%f.
)
pause

