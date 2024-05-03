const fileInput = document.getElementById('customFile');
const analyseDiv = document.getElementById('analyse')
const mainDiv = document.getElementById('main')
let mainBtnGroup = null
let errorHdiv = null
let okDiv = null
function createOkLabel(){
    okDiv = document.createElement('div')
    okDiv.className = 'col-sm-6'
    okDiv.style = 'margin-block: 10px'

    let ok = document.createElement('h4')
    ok.innerText = 'В документах отсутствуют ошибки'
    okDiv.append(ok)

    let btnGroup = document.createElement('div')
    btnGroup.className = 'btn-group-vertical'

    mainDiv.append(okDiv)
    mainDiv.append(btnGroup)
    mainBtnGroup = btnGroup
}
function createValidationLabel(){
    errorHdiv = document.createElement('div')
    errorHdiv.className = 'col-sm-6'
    errorHdiv.style = 'margin-block: 10px'

    let errorH = document.createElement('h4')
    errorH.innerText = 'Ошибки валидации'
    errorHdiv.append(errorH)

    let btnGroup = document.createElement('div')
    btnGroup.className = 'btn-group-vertical'

    mainDiv.append(errorHdiv)
    mainDiv.append(btnGroup)
    mainBtnGroup = btnGroup
}
function createErrorLabel(){
    errorHdiv = document.createElement('div')
    errorHdiv.className = 'col-sm-6'
    errorHdiv.style = 'margin-block: 10px'

    let errorH = document.createElement('h4')
    errorH.innerText = 'Ошибки в документах'
    errorHdiv.append(errorH)

    let btnGroup = document.createElement('div')
    btnGroup.className = 'btn-group-vertical'

    mainDiv.append(errorHdiv)
    mainDiv.append(btnGroup)
    mainBtnGroup = btnGroup
}
function createMultipleErrorLabel(error, subError, docs){
   const btnGroup = document.createElement('div');
   btnGroup.classList.add('btn-group', 'col-sm-6');

   const button = document.createElement('button');
   button.setAttribute('type', 'button');
   button.classList.add('btn', 'btn-warning', 'dropdown-toggle');
   button.setAttribute('data-bs-toggle', 'dropdown');
   button.setAttribute('aria-expanded', 'false');
   button.innerHTML = error + '('+docs.length+')';

   const dropdownMenu = document.createElement('ul');
   dropdownMenu.classList.add('dropdown-menu');

   docs.forEach((doc)=>{
        const divider = document.createElement("hr");
        divider.classList.add("dropdown-divider");

        const li = document.createElement("li");
        li.appendChild(divider);
        dropdownMenu.appendChild(li);

        const listItem1 = document.createElement('li');
        const link1 = document.createElement('a');
        link1.classList.add('dropdown-item');
        link1.setAttribute('href', '#');
        link1.innerText = "Неверное название документа в директории с ФОС документами: 'Документ'";
        link1.innerText = subError + " '"+doc+"'";
        link1.style = 'text-wrap: pretty;'

        listItem1.appendChild(link1);

        dropdownMenu.appendChild(listItem1);
   })

   btnGroup.appendChild(button);
   btnGroup.appendChild(dropdownMenu);

   return btnGroup
}

function createStandardErrorLabel(error, subError){
    const btnGroup = document.createElement('div');
    btnGroup.classList.add('btn-group', 'col-sm-6');

    const button = document.createElement('button');
    button.setAttribute('type', 'button');
    button.classList.add('btn', 'btn-warning', 'dropdown-toggle');
    button.setAttribute('data-bs-toggle', 'dropdown');
    button.setAttribute('aria-expanded', 'false');
    button.textContent = error;

    const dropdownMenu = document.createElement('ul');
    dropdownMenu.classList.add('dropdown-menu');

    const listItem = document.createElement('li');
    const link = document.createElement('a');
    link.classList.add('dropdown-item');
    link.setAttribute('href', '#');
    link.textContent = subError;

    listItem.appendChild(link);
    dropdownMenu.appendChild(listItem);

    btnGroup.appendChild(button);
    btnGroup.appendChild(dropdownMenu);

    return btnGroup
}

function createComplianceError(errors, errorType){
   const btnGroup = document.createElement('div');
   btnGroup.classList.add('btn-group', 'col-sm-6');

   const button = document.createElement('button');
   button.setAttribute('type', 'button');
   button.classList.add('btn', 'btn-danger', 'dropdown-toggle');
   button.setAttribute('data-bs-toggle', 'dropdown');
   button.setAttribute('aria-expanded', 'false');
   button.innerHTML = errorType + " (" + errors.length + ")";

   const dropdownMenu = document.createElement('ul');
   dropdownMenu.classList.add('dropdown-menu');

   errors.forEach((error)=>{
        const divider = document.createElement("hr");
        divider.classList.add("dropdown-divider");

        const li = document.createElement("li");
        li.appendChild(divider);
        dropdownMenu.appendChild(li);

        const listItem1 = document.createElement('li');
        const link1 = document.createElement('a');
        link1.classList.add('dropdown-item');
        link1.setAttribute('href', '#');
        link1.innerHTML = error;
        link1.style = 'text-wrap: pretty;'

        listItem1.appendChild(link1);

        dropdownMenu.appendChild(listItem1);
   })

   btnGroup.appendChild(button);
   btnGroup.appendChild(dropdownMenu);

   return btnGroup
}


$(function () {
  $('[data-toggle="tooltip"]').tooltip({html: true, placement: "bottom"})
})

fileInput.addEventListener('change', handleFileSelect);
function handleFileSelect(event) {
    analyseDiv.style.removeProperty('display')
    if(errorHdiv){
        mainDiv.removeChild(errorHdiv)
        mainDiv.removeChild(mainBtnGroup)
    }
    if(okDiv){
        mainDiv.removeChild(okDiv)
        mainDiv.removeChild(mainBtnGroup)
        okDiv = null
    }
    const file = event.target.files[0];
    const formData = new FormData();
    formData.append('file', file);

    fetch('http://localhost:8080/upload-documents', {
        method: 'POST',
        body: formData
    })
     .then(function (response) {
        return response.json()
      })
      .then(function (data) {
        console.log(data)
        analyseDiv.style.cssText = "display: none !important;";

        if (!data.valid){
            createValidationLabel()
            if(!data.characteristicsFound){
                mainBtnGroup.append(createStandardErrorLabel('Характеристика не найдена',
                 "Не найден документ 'Характеристика ОПОП'"))
            }
            else{
                if(!data.characteristicsInRightExtension){
                        mainBtnGroup.append(createStandardErrorLabel('Формат характеристики',
                             'Характеристика должна быть в формате docx'))
                 }
            }

            if(!data.fosPackageFound){
                mainBtnGroup.append(createStandardErrorLabel('Не найдена директория с документами ФОС',
                 "Не найдена директория с документами ФОС, директория должна называться 'Оценочные средства (ФОС)'"))

            }
            if(!data.rpdPackageFound){
                mainBtnGroup.append(createStandardErrorLabel('Не найдена директория с документами РПД',
                   "Не найдена директория с документами РПД, директория должна называться 'Рабочие программы дисциплин'"))

            }
            if(!data.syllabusFound){
                mainBtnGroup.append(createStandardErrorLabel('Учебный план не найден',
                    "Не найден документ с учебным планом"))

            }
            else{
                if(!data.syllabusInRightExtension){
                        mainBtnGroup.append(createStandardErrorLabel('Формат учебного плана',
                          "Учебный план должен быть в формате xls/xlsx"))
                }
            }

            if(data.fosInWrongFormat.length>0){
                mainBtnGroup.append(createMultipleErrorLabel('Название документов ФОС ',
                       "Неверное название или формат документа в директории с ФОС документами: ",
                       data.fosInWrongFormat))
            }
            if(data.rpdInWrongFormat.length>0){
                 mainBtnGroup.append(createMultipleErrorLabel('Название документов РПД ',
                       "Неверное название или формат документа в директории с РПД документами: ",
                       data.rpdInWrongFormat))
            }

            if(data.unknownDocuments.length>0){
                  mainBtnGroup.append(createMultipleErrorLabel('Неизвестные документы ',
                        "Неизвестный документ: ",
                        data.unknownDocuments))
            }
        }
        else{

            if(data.ok){
                createOkLabel()
            }
            else{
                createErrorLabel()
                if(data.characteristicErrors.length>0){
                    mainBtnGroup.append(createComplianceError(data.characteristicErrors, "Ошибки в характеристике"))
                }
                if(data.fosErrors.length>0){
                    mainBtnGroup.append(createComplianceError(data.fosErrors, "Ошибки в ФОС"))
                }
            }
        }

      })
      .catch(function (error) {
        console.log('error', error)
      })
}