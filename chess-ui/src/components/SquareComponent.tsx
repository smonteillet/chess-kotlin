import styled from 'styled-components';
import Theme from '../ui/Theme';
import Size from '../ui/Size';
import {Square} from '../domain/Square';
import {Rank, ranks} from '../domain/Rank';
import {File, files} from '../domain/File';
import React from 'react';

export type SquareComponentProps = {
    square: Square
}

const SquareComponentContainer = styled.div<SquareComponentProps>`
  background-color: ${(props: SquareComponentProps) => isDarkSquare(props.square) ? Theme.darkSquare : Theme.lightSquare};
  width: ${Size.squareSize};
  height: ${Size.squareSize};
  color: #000;
  display: flex;
  z-index: -1;
  position: relative;
`

const SquareFileIndicator = styled.div<SquareComponentProps>`
  color: ${(props: SquareComponentProps) => isDarkSquare(props.square) ? Theme.lightSquare : Theme.darkSquare};
  display: flex;
  align-items: flex-end;
  padding-left: 3px;
  width: 100%;
`

const SquareRankIndicator = styled.div<SquareComponentProps>`
  color: ${(props: SquareComponentProps) => isDarkSquare(props.square) ? Theme.lightSquare : Theme.darkSquare};
  display: flex;
  justify-content: flex-end;
  padding-right: 3px;
  width: 100%;
`

const H1SquareFileIndicator = styled.div<SquareComponentProps>`
  color: ${(props: SquareComponentProps) => isDarkSquare(props.square) ? Theme.lightSquare : Theme.darkSquare};
  display: flex;
  align-items: flex-end;
  padding-left: 3px;
  width: 50%;
`

const H1SquareRankIndicator = styled.div<SquareComponentProps>`
  color: ${(props: SquareComponentProps) => isDarkSquare(props.square) ? Theme.lightSquare : Theme.darkSquare};
  display: flex;
  justify-content: flex-end;
  padding-right: 3px;
  width: 50%;
`



const isDarkSquare = (square : Square) => {
    const rankIndex = ranks.indexOf(parseInt(square[1]) as Rank)
    const fileIndex = files.indexOf(square[0] as File)
    return rankIndex % 2 == 0 && fileIndex % 2 == 0 || rankIndex % 2 != 0 && fileIndex % 2 != 0
}

const SquareComponent : React.FC<SquareComponentProps> = (props) => {
    const rank = props.square[1]
    const file = props.square[0]
    const isRank1 = rank === '1'
    const isFileH = file === 'h'
    return <SquareComponentContainer square={props.square}>
        {isRank1 && !isFileH && <SquareFileIndicator square={props.square}>{file}</SquareFileIndicator>}
        {isFileH && !isRank1 && <SquareRankIndicator square={props.square}>{rank}</SquareRankIndicator>}
        {isFileH && isRank1 && <>
            <H1SquareFileIndicator square={props.square}>h</H1SquareFileIndicator>
            <H1SquareRankIndicator square={props.square}>1</H1SquareRankIndicator>
        </>
        }
    </SquareComponentContainer>
}

export default SquareComponent